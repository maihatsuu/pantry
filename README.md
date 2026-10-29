# Pantry

A small Fabric mod for Minecraft 1.21.1 that makes food and potions a little nicer. Everything else is vanilla.

Requires [Fabric Loader](https://fabricmc.net/use/) 0.20.1+ and [Fabric API](https://modrinth.com/mod/fabric-api). Must be installed on both the server and the client.

## Features

### Food bonus effects
Some foods grant an extra effect. Like vanilla food effects, they aren't shown in the tooltip.

| Food | Effect |
|---|---|
| Pumpkin Pie | Haste II, 30s |
| Beetroot Soup | Strength I, 30s |
| Cookie | Speed II, 30s |
| Glow Berries | Glowing, 30s |
| Rabbit Stew | Luck II, 30s |
| Golden Carrot | Night Vision, 30s |
| Mushroom Stew | Absorption I, 30s |
| Baked Potato | Resistance I, 30s |

### Always edible
Every food can be eaten even when you're full, like golden apples in vanilla.

### Faster snacks
Cookies, Sweet Berries, Glow Berries, Apples, Carrots and Melon Slices take 0.8s to eat instead of 1.6s, like Dried Kelp.

### Stacking
Soups, stews and potions (drinkable, splash and lingering) stack to 16, and cakes stack to 64. Eating from a stack returns the bowl to your inventory.

## Building

```bash
./gradlew build       # jar in build/libs/
./gradlew runClient   # dev client with the mod loaded
```

The project uses Mojang's official mappings and builds against Java 21.

## License

CC0-1.0. See [LICENSE](LICENSE).
