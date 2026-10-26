#!/usr/bin/env python3
"""Generates src/main/resources/data/satiated/foods.json from the balance CSVs.

    python3 balance/generate_foods.py

Group Foods.csv       one column per group; the cells below each header are its members.
Group Satiation.csv   Group, Satiated Level, Satiated Duration
Individual Foods.csv  Item, Satiated Level, Satiated Duration, Bonus Effect, Bonus Level, Bonus Duration
                      (blank level/duration = use the group's; one row per bonus effect)

Items can be in-game names ("Steak", "Raw Cod"), ids ("minecraft:cooked_beef", "mymod:pasta")
or item tags ("#c:foods/cooked_meats"). Effects can be names ("Night Vision") or ids.
Levels are written as in-game (Haste 2 = Haste II). Durations accept 30s, 5m, 1m30s, 1:30 or plain seconds.
"""

import csv
import json
import re
import sys
from pathlib import Path

BALANCE = Path(__file__).resolve().parent
OUTPUT = BALANCE.parent / "src/main/resources/data/satiated/foods.json"

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


def main():
    # Group Foods.csv: headers are group names, cells below are members.
    group_table = rows("Group Foods.csv")
    headers = group_table[0]
    members = {name: [] for name in headers if name}
    seen = {}
    for row in group_table[1:]:
        for name, cell in zip(headers, row):
            if not name or not cell:
                continue
            member = cell if cell.startswith("#") else food_id(cell)
            if member in seen and seen[member] != name:
                errors.append(f"'{cell}' is in both '{seen[member]}' and '{name}'")
            seen[member] = name
            members[name].append(member)

    groups = {}
    for row in rows("Group Satiation.csv")[1:]:
        if not any(row):
            continue
        name, tier, duration = row[:3]
        if name not in members:
            errors.append(f"Group Satiation.csv: '{name}' isn't a column in Group Foods.csv")
            continue
        groups[slug(name)] = {
            "tier": level(tier, name),
            "duration": ticks(duration, name),
            "items": members[name],
        }
    for name in members:
        if slug(name) not in groups:
            errors.append(f"group '{name}' has no row in Group Satiation.csv")

    items = {}
    for row in rows("Individual Foods.csv")[1:]:
        if not any(row):
            continue
        row += [""] * (6 - len(row))
        name, tier, duration, effect, effect_level, effect_duration = row[:6]
        item = food_id(name)
        entry = items.setdefault(item, {})
        if tier:
            entry["tier"] = level(tier, name)
        if duration:
            entry["duration"] = ticks(duration, name)
        if effect:
            instance = {"id": effect_id(effect), "duration": ticks(effect_duration, f"{name} bonus")}
            amplifier = level(effect_level or "1", f"{name} bonus") - 1
            if amplifier:
                instance["amplifier"] = amplifier
            entry.setdefault("effects", []).append({"effect": instance})
        if item not in seen and ("tier" not in entry or "duration" not in entry):
            print(f"note: '{name}' is in no group, so its missing tier/duration use the formula fallback")

    for food in sorted(VANILLA_FOODS - {m.removeprefix("minecraft:") for m in seen}):
        print(f"note: minecraft:{food} is in no group and will use the formula fallback")

    if errors:
        print("foods.json was not written:", *(f"  - {e}" for e in errors), sep="\n", file=sys.stderr)
        sys.exit(1)

    OUTPUT.write_text(json.dumps({"groups": groups, "items": items}, indent="\t") + "\n", encoding="utf-8")
    print(f"Wrote {len(groups)} groups and {len(items)} item entries to {OUTPUT.relative_to(BALANCE.parent)}")


if __name__ == "__main__":
    main()
