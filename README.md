# Satiated

A Fabric mod for Minecraft 1.21.1 that replaces the hunger bar with food-granted regeneration and rebalances potions around it.

Requires [Fabric Loader](https://fabricmc.net/use/) 0.20.1+ and [Fabric API](https://modrinth.com/mod/fabric-api). Must be installed on both the server and the client.

## Features

### Food
- **No hunger bar.** Hunger never drains, there is no natural regeneration, and you can't starve.
- **Food grants Satiated**, a regeneration effect whose tier and duration depend on the food. The heal rate goes up with tier (tier I heals 1 HP every 5s, II every 2.5s, III every 1.25s).
- **Food is always edible.** Eating while Satiated keeps whichever effect is stronger: a higher tier replaces the current one, and the same tier keeps the longer duration.
- **Some foods grant extra effects** on top of Satiated, set in the food table.
- **Sprinting is locked** at 3 hearts or below.
- **Soups and stews stack to 16.** Eating from a stack returns the bowl to your inventory.
- **Hunger** (e.g. from rotten flesh) drains Satiated faster. **Saturation** extends it.
- Food tooltips show the Satiated tier and duration they grant.

### Potions
- **Drinkable potions last longer**, up to 30 minutes with redstone. Splash and lingering potions keep vanilla durations.
- **Potions stack to 16** (drinkable, splash and lingering).
- **Healing and regeneration can't be brewed.** They can still be obtained in creative or with commands.

## Configuration

Balance is data-driven. Both tables are datapack files and can be overridden by any datapack; `/reload` applies changes.

### `data/satiated/foods.json`

```json
{
  "foods": {
    "minecraft:cooked_beef": { "tier": 3, "duration": 6240 },
    "minecraft:golden_carrot": {
      "tier": 2,
      "duration": 6120,
      "effects": [
        { "effect": { "id": "minecraft:night_vision", "duration": 1200 }, "probability": 1.0 }
      ],
      "replace_vanilla_effects": false
    }
  }
}
```

| Field | Description |
|---|---|
| `tier` | Satiated tier, starting at 1. |
| `duration` | Satiated duration in ticks (20 ticks = 1 second). |
| `effects` | Optional. Extra effects, in the same format as vanilla food effects. `probability` defaults to 1.0. |
| `replace_vanilla_effects` | Optional. If `true`, the food's built-in effects (e.g. rotten flesh's Hunger) are not applied. |

Cake is keyed as `minecraft:cake` and applies per slice. Foods missing from the table, including modded foods, get a tier and duration calculated from their vanilla nutrition and saturation.

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

Entries from datapacks are merged on top of the mod's defaults, so a datapack only needs the entries it changes. Add `"replace": true` at the top level of a file to discard every entry loaded before it.

## Building

```bash
./gradlew build       # jar in build/libs/
./gradlew runClient   # dev client with the mod loaded
```

The project uses Mojang's official mappings and builds against Java 21.

## License

CC0-1.0. See [LICENSE](LICENSE).
