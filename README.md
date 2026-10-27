# Satiated

A Fabric mod for Minecraft 1.21.1 that replaces the hunger bar with food-based healing and rebalances potions around it.

Requires [Fabric Loader](https://fabricmc.net/use/) 0.20.1+ and [Fabric API](https://modrinth.com/mod/fabric-api). Must be installed on both the server and the client.

## Features

### Food
- **No hunger bar.** Hunger never drains, there is no natural regeneration, and you can't starve.
- **Food queues healing.** Each food heals a set number of hearts at one of three speeds: half a heart every 10s, every 5s, or every second.
- **The queue shows on the health bar** as faded hearts in the slots it will refill. They turn solid as you heal.
- **Eating replaces the queue.** Your new food's healing and speed replace whatever was queued, and never add to it, so what you eat is a choice.
- **The queue runs down even at full health**, so healing can't be banked indefinitely.
- **Some foods grant bonus effects**, set in the food table. Food is always edible.
- **Sprinting is locked** at 3 hearts or below.
- **Soups and stews stack to 16.** Eating from a stack returns the bowl to your inventory.
- **Hunger** (e.g. from rotten flesh) pauses the queue until it wears off. **Saturation** heals instantly.
- Food tooltips show how much a food heals and how fast, e.g. "Heals 6 ❤ · ½ ❤ every 5s", plus any bonus effects.

### Potions
- **Drinkable potions last longer**, up to 30 minutes with redstone. Splash and lingering potions keep vanilla durations.
- **Potions stack to 16** (drinkable, splash and lingering).
- **Healing and regeneration can't be brewed.** They can still be obtained in creative or with commands.

## Configuration

Balance is data-driven. Both tables are datapack files and can be overridden by any datapack; `/reload` applies changes.

### `data/satiated/foods.json`

Foods are sorted into **groups**. Every food in a group heals the same amount at the same speed. Individual foods can add bonus effects or override their group.

```json
{
  "groups": {
    "cooked_meat": {
      "speed": 2,
      "hearts": 12,
      "items": ["minecraft:cooked_beef", "minecraft:cooked_porkchop", "#c:foods/cooked_meats"]
    }
  },
  "items": {
    "minecraft:golden_carrot": {
      "effects": [
        { "effect": { "id": "minecraft:night_vision", "duration": 1200 } }
      ]
    }
  }
}
```

| Group field | Description |
|---|---|
| `speed` | 1 heals half a heart every 10s, 2 every 5s, 3 every second. |
| `hearts` | Total healing, in multiples of 0.5 (e.g. `6` or `2.5`). |
| `items` | Item ids, or item tags starting with `#`. Tags let modded foods join a group automatically. |

| Item field | Description |
|---|---|
| `effects` | Optional. Bonus effects, in the same format as vanilla food effects. `amplifier` is the level minus one, and `probability` defaults to 1.0. |
| `speed`, `hearts` | Optional. Override the item's group. |
| `replace_vanilla_effects` | Optional. If `true`, the food's built-in effects (e.g. rotten flesh's Hunger) are not applied. |

A food takes its speed and hearts from its own `items` entry first, then a group that lists it by id, then a group that matches it by tag. If it matches none of those, it heals its vanilla nutrition in half-hearts, faster for more filling foods. If a food is in several groups the same way, the last group loaded wins. Cake is keyed as `minecraft:cake` and applies per slice.

#### Editing the balance spreadsheets

The shipped `foods.json` is generated from the CSVs in [`balance/`](balance), which are the source of truth. Edit the CSVs, then run:

```bash
python3 balance/generate_foods.py
```

Groups and overrides use **Speed** (1–3) and **Hearts** columns. The CSVs accept in-game names ("Steak", "Raw Cod"), item ids, or `#tags`. Bonus effect durations can be written like `30s`, `5m` or `1:30`, and bonus levels as in-game, so Haste 2 is Haste II. The script lists every problem it finds, such as unknown names, foods in two groups, or invalid speeds and hearts, and doesn't write the file until they're fixed.

### `data/satiated/potion_durations.json`

```json
{
  "potions": {
    "minecraft:swiftness": 3600,
    "minecraft:long_swiftness": 36000
  }
}
```

Maps a potion id to its duration in ticks when drunk. The duration applies to every effect of that potion. Potions not listed keep vanilla durations.

### Overriding from a datapack

Entries from datapacks are merged on top of the mod's defaults (per group name, item id or potion id), so a datapack only needs the entries it changes. Redefining a group replaces its whole item list. Add `"replace": true` at the top level of a file to discard every entry loaded before it.

## Building

```bash
./gradlew build       # jar in build/libs/
./gradlew runClient   # dev client with the mod loaded
```

The project uses Mojang's official mappings and builds against Java 21.

## License

CC0-1.0. See [LICENSE](LICENSE).
