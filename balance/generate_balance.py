#!/usr/bin/env python3
"""Generates the mod's balance data from the CSVs in this folder.

    python3 balance/generate_balance.py

Food Effects.csv      Item, Bonus Effect, Bonus Level, Bonus Duration  (one row per bonus effect)
                      -> src/main/resources/data/satiated/foods.json
Exhaustion.csv        Source, Description, Vanilla Cost, Cost
                      -> src/main/resources/data/satiated/exhaustion.json

Items can be in-game names ("Steak", "Raw Cod") or ids ("minecraft:cooked_beef", "mymod:pasta").
Effects can be names ("Night Vision") or ids. Bonus levels are written as in-game (Haste 2 = Haste II).
Durations accept 30s, 5m, 1m30s, 1:30 or plain seconds.

Exhaustion costs are in vanilla's units: every 4 exhaustion drains one point of saturation, or of hunger
once saturation is empty. 0 makes an action free. Nothing is written if any row has a problem.
"""

import csv
import json
import re
import sys
from pathlib import Path

BALANCE = Path(__file__).resolve().parent
DATA = BALANCE.parent / "src/main/resources/data/satiated"

VANILLA_FOODS = {
    "apple", "baked_potato", "beef", "beetroot", "beetroot_soup", "bread", "cake", "carrot", "chicken",
    "chorus_fruit", "cod", "cooked_beef", "cooked_chicken", "cooked_cod", "cooked_mutton", "cooked_porkchop",
    "cooked_rabbit", "cooked_salmon", "cookie", "dried_kelp", "enchanted_golden_apple", "glow_berries",
    "golden_apple", "golden_carrot", "honey_bottle", "melon_slice", "mushroom_stew", "mutton",
    "poisonous_potato", "porkchop", "potato", "pufferfish", "pumpkin_pie", "rabbit", "rabbit_stew",
    "rotten_flesh", "salmon", "spider_eye", "suspicious_stew", "sweet_berries", "tropical_fish",
}

# In-game names that don't turn into the item id by lowercasing them.
FOOD_NAMES = {
    "steak": "cooked_beef",
    "raw beef": "beef",
    "raw porkchop": "porkchop",
    "raw chicken": "chicken",
    "raw mutton": "mutton",
    "raw rabbit": "rabbit",
    "raw cod": "cod",
    "raw salmon": "salmon",
}

VANILLA_EFFECTS = {
    "speed", "slowness", "haste", "mining_fatigue", "strength", "instant_health", "instant_damage",
    "jump_boost", "nausea", "regeneration", "resistance", "fire_resistance", "water_breathing",
    "invisibility", "blindness", "night_vision", "hunger", "weakness", "poison", "wither", "health_boost",
    "absorption", "saturation", "glowing", "levitation", "luck", "unluck", "slow_falling", "conduit_power",
    "dolphins_grace", "bad_omen", "hero_of_the_village", "darkness", "trial_omen", "raid_omen",
    "wind_charged", "weaving", "oozing", "infested",
}

EFFECT_NAMES = {"bad luck": "unluck", "instant damage": "instant_damage", "jump boost": "jump_boost"}

errors = []


def slug(text):
    return re.sub(r"[^a-z0-9]+", "_", text.strip().lower().replace("'", "")).strip("_")


def resolve(name, names, known, kind):
    """Turns an in-game name or id into a namespaced id, recording an error if it isn't recognised."""
    name = name.strip()
    if ":" in name:
        return name
    key = names.get(name.lower(), slug(name))
    if key not in known:
        errors.append(f"unknown {kind} '{name}' (use a namespaced id for modded {kind}s)")
    return f"minecraft:{key}"


def food_id(name):
    return resolve(name, FOOD_NAMES, VANILLA_FOODS, "food")


def effect_id(name):
    return resolve(name, EFFECT_NAMES, VANILLA_EFFECTS, "effect")


def ticks(text, where):
    text = text.strip().lower()
    if re.fullmatch(r"\d+:\d{1,2}", text):
        minutes, seconds = text.split(":")
        return (int(minutes) * 60 + int(seconds)) * 20
    if re.fullmatch(r"\d+(\.\d+)?", text):
        return round(float(text) * 20)
    match = re.fullmatch(r"(?:(\d+(?:\.\d+)?)m)?\s*(?:(\d+(?:\.\d+)?)s)?", text)
    if not text or not match:
        errors.append(f"{where}: can't read duration '{text}'")
        return 0
    return round((float(match[1] or 0) * 60 + float(match[2] or 0)) * 20)


def level(text, where):
    text = text.strip()
    if not text.isdigit() or int(text) < 1:
        errors.append(f"{where}: level must be a whole number from 1, got '{text}'")
        return 1
    return int(text)


def rows(file):
    with open(BALANCE / file, newline="", encoding="utf-8-sig") as f:
        return [[cell.strip() for cell in row] for row in csv.reader(f)]


EXHAUSTION_SOURCES = {
    "walk", "crouch", "sprint", "wade", "walk_underwater", "swim", "jump", "sprint_jump",
    "mine", "attack", "damage", "heal", "hunger_effect",
}


def cost(text, where):
    try:
        value = float(text.strip())
    except ValueError:
        value = -1
    if value < 0:
        errors.append(f"{where}: cost must be a number from 0, got '{text.strip()}'")
        return 0
    return int(value) if value == int(value) else value


def foods():
    items = {}
    for row in rows("Food Effects.csv")[1:]:
        if not any(row):
            continue
        row += [""] * (4 - len(row))
        name, effect, effect_level, effect_duration = row[:4]
        entry = items.setdefault(food_id(name), {})
        if effect:
            instance = {"id": effect_id(effect), "duration": ticks(effect_duration, f"{name} bonus")}
            amplifier = level(effect_level or "1", f"{name} bonus") - 1
            if amplifier:
                instance["amplifier"] = amplifier
            entry.setdefault("effects", []).append({"effect": instance})
    return {"items": items}


def exhaustion():
    costs = {}
    for row in rows("Exhaustion.csv")[1:]:
        if not any(row):
            continue
        source = row[0].strip()
        if source not in EXHAUSTION_SOURCES:
            errors.append(f"Exhaustion.csv: unknown source '{source}' (known: {', '.join(sorted(EXHAUSTION_SOURCES))})")
            continue
        if source in costs:
            errors.append(f"Exhaustion.csv: '{source}' is listed twice")
        costs[source] = cost(row[3] if len(row) > 3 else "", source)
    for source in sorted(EXHAUSTION_SOURCES - costs.keys()):
        print(f"note: '{source}' isn't in Exhaustion.csv and keeps its vanilla cost")
    return {"costs": costs}


def main():
    outputs = {"foods.json": foods(), "exhaustion.json": exhaustion()}
    if errors:
        print("Nothing was written:", *(f"  - {e}" for e in errors), sep="\n", file=sys.stderr)
        sys.exit(1)

    for name, data in outputs.items():
        (DATA / name).write_text(json.dumps(data, indent="\t") + "\n", encoding="utf-8")
    print(f"Wrote {len(outputs['foods.json']['items'])} food entries and {len(outputs['exhaustion.json']['costs'])} exhaustion costs")


if __name__ == "__main__":
    main()
