# Satiated

A Fabric mod for Minecraft 1.21.1 that keeps vanilla's hunger bar but lets you tune what makes you hungry, gives foods bonus effects, and makes potions last longer.

Requires [Fabric Loader](https://fabricmc.net/use/) 0.20.1+ and [Fabric API](https://modrinth.com/mod/fabric-api). Must be installed on both the server and the client.

## Features

### Food
- **Vanilla hunger and natural regeneration**, with every source of hunger tunable: walking, sprinting, swimming, jumping, mining, attacking, taking damage, healing and the Hunger effect.
- **Tunable regeneration**: how full the hunger bar must be to heal, and how often it heals.
- **Some foods grant bonus effects**, e.g. Haste II from pumpkin pie. They're shown in the food's tooltip.
- **Soups and stews stack to 16.** Eating from a stack returns the bowl to your inventory.

### Potions
- **Drinkable potions last longer**, up to 30 minutes with redstone. Splash and lingering potions keep vanilla durations.
- **Potions stack to 16** (drinkable, splash and lingering).

## Configuration

Balance is data-driven. All four tables are datapack files and can be overridden by any datapack; `/reload` applies changes.

### `data/satiated/exhaustion.json`

```json
{
  "costs": {
    "sprint": 0.1,
    "heal": 6
  }
}
```

Every action adds *exhaustion*, and every 4 exhaustion drains one point of saturation, or of hunger once saturation is empty. Each cost is in vanilla's units, and `0` makes an action free. Sources not listed keep their vanilla cost.

| Source | Vanilla | Per |
|---|---|---|
| `walk`, `crouch` | 0 | meter |
| `sprint` | 0.1 | meter |
| `wade`, `walk_underwater`, `swim` | 0.01 | meter |
| `jump` | 0.05 | jump |
| `sprint_jump` | 0.2 | jump |
| `mine` | 0.005 | block broken |
| `attack` | 0.1 | attack |
| `damage` | 0.1 | hit taken (scaled by damage type, as in vanilla) |
| `heal` | 6 | half-heart of natural regeneration |
| `hunger_effect` | 0.005 | tick, per level of Hunger |

### `data/satiated/regeneration.json`

```json
{
  "settings": {
    "slow_regen_min_hunger": 18,
    "slow_regen_interval": 80
  }
}
```

Vanilla heals in two ways. Fast regen runs while the bar is full and saturation is above 0, and pays from saturation. Slow regen heals half a heart at a time while the bar is nearly full. Below that, nothing heals. Settings not listed keep their vanilla value.

| Setting | Vanilla | Meaning |
|---|---|---|
| `fast_regen_min_hunger` | 20 | Hunger points (20 = full bar) needed for fast regen |
| `fast_regen_interval` | 10 | Ticks between fast-regen heals |
| `slow_regen_min_hunger` | 18 | Hunger points needed for slow regen |
| `slow_regen_interval` | 80 | Ticks between slow-regen heals |

Each heal costs the `heal` exhaustion per half-heart restored.

### `data/satiated/foods.json`

```json
{
  "items": {
    "minecraft:golden_carrot": {
      "effects": [
        { "effect": { "id": "minecraft:night_vision", "duration": 1200 } }
      ]
    }
  }
}
```

| Field | Description |
|---|---|
| `effects` | Bonus effects, in the same format as vanilla food effects. `amplifier` is the level minus one, and `probability` defaults to 1.0. |
| `replace_vanilla_effects` | Optional. If `true`, the food's built-in effects (e.g. rotten flesh's Hunger) are not applied. |

Cake is keyed as `minecraft:cake` and applies per slice. Hunger and saturation values are vanilla.

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

### Editing the balance spreadsheets

The shipped `foods.json`, `exhaustion.json` and `regeneration.json` are generated from the CSVs in [`balance/`](balance), which are the source of truth. Edit the CSVs, then run:

```bash
python3 balance/generate_balance.py
```

- `Individual Foods.csv` has one row per bonus effect. It accepts in-game names ("Steak", "Raw Cod") or ids, durations like `30s`, `5m` or `1:30`, and levels as in-game, so Haste 2 is Haste II.
- `Exhaustion.csv` lists every source with its vanilla cost for reference. Edit the **Cost** column.
- `Regeneration.csv` takes hunger points (0 to 20) and durations like `0.5s` or `4s`. Edit the **Value** column.

The script lists every problem it finds, such as unknown names or sources, or values it can't read, and doesn't write anything until they're fixed.

### Overriding from a datapack

Entries from datapacks are merged on top of the mod's defaults (per item, potion, exhaustion source or regeneration setting), so a datapack only needs the entries it changes. Add `"replace": true` at the top level of a file to discard every entry loaded before it.

## Building

```bash
./gradlew build       # jar in build/libs/
./gradlew runClient   # dev client with the mod loaded
```

The project uses Mojang's official mappings and builds against Java 21.

## License

CC0-1.0. See [LICENSE](LICENSE).
