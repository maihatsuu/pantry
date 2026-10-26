# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Satiated is a Fabric mod for Minecraft 1.21.1 (mod id `satiated`, package `maihatsuu.satiated`) that overhauls food and potions. The hunger bar is removed, and food instead grants a tiered "Satiated" regeneration effect. Sprinting is locked at 3 hearts or below. Drinkable potions get longer durations from a table, and healing/regeneration can't be brewed. Soups, stews and potions stack to 16. `fabric.mod.json` still has the template's authors and contact links.

Versions (Minecraft, Fabric Loader, Loom, Fabric API) and the mod version live in `gradle.properties`. Java 21 is the compile target. CI (`.github/workflows/build.yml`) runs `./gradlew build` on JDK 25.

## Commands

```bash
./gradlew build          # compile + remap; jars land in build/libs/
./gradlew runClient      # launch a dev Minecraft client with the mod loaded
./gradlew runServer      # launch a dev dedicated server
./gradlew genSources     # decompile Minecraft sources for browsing/searching
```

No test suite is configured.

## Architecture

- **Mojang official mappings**: `build.gradle` uses `loom.officialMojangMappings()`, not Yarn. Use Mojang names (`ResourceLocation`, `MinecraftServer.loadLevel`, `net.minecraft.client.Minecraft`, etc.). Yarn names from Fabric docs or other mods will not compile here.
- **Split source sets** (`splitEnvironmentSourceSets()`):
  - `src/main/`: common/server-safe code. Entrypoint `Satiated` (`ModInitializer`) registers everything and defines `MOD_ID`, `LOGGER`, and the `Satiated.id(path)` helper for namespaced `ResourceLocation`s.
  - `src/client/`: client-only code. Entrypoint `client.SatiatedClient` (`ClientModInitializer`) holds the sync receiver and the food tooltip; its mixins hide the hunger HUD and gate sprinting on health. Client code can reference main, but main must never reference client classes.
- **Mixins**: there are two configs, and each new mixin class has to be registered in the right one:
  - `src/main/resources/satiated.mixins.json` (package `maihatsuu.satiated.mixin`, `mixins` array)
  - `src/client/resources/satiated.client.mixins.json` (package `maihatsuu.satiated.client.mixin`, `client` array)
  - Both set `defaultRequire: 1`, so an injector that fails to match its target crashes at startup instead of being silently skipped.
- **Assets/data** go under `src/main/resources/assets/satiated/` and `data/satiated/`. `fabric.mod.json` has `${version}` substituted from `gradle.properties` during `processResources`.
- **`./gradlew runServer`** stops at the EULA prompt until `run/eula.txt` is set to `true`. Most mixin targets only load once a world starts, so a passing `build` doesn't prove the injectors match.

## How the overhaul fits together

- **Balance lives in datapack JSON, not code**:
  - `data/satiated/foods.json` has `groups` (name → `tier`, `duration` in ticks, `items` as ids or `#tags`) and `items` (id → optional `tier`/`duration` overrides, bonus `effects` in vanilla `FoodProperties.PossibleEffect` shape, `replace_vanilla_effects`). It's loaded by `food/FoodTable`.
  - `data/satiated/potion_durations.json` maps potion id → drinkable duration in ticks. It's loaded by the generic `data/MergedJsonTable`.
  - Both merge every datapack's copy in pack order, and `"replace": true` clears earlier packs.
- **`foods.json` is generated; don't hand-edit it.** The source of truth is the CSVs in `balance/`. After changing them, run `python3 balance/generate_foods.py`, which validates names, durations and duplicate group membership before writing.
- **Food resolution** (`SatiatedFoods.resolve`): the item's `items` override comes first, then a group listing it by id, then a group matching it by tag (last loaded wins ties), then `SatiatedFoods.fallback` computed from nutrition and saturation. Tags are matched at lookup time rather than load time, because the server binds tags after reload listeners run. `FoodTable` keeps groups in insertion order, and so does the sync payload's `LinkedHashMap`, because that order decides ties.
- **Tables are synced to clients** by `network/SyncTablesPayload` on join and after `/reload`, because the food and potion tooltips read them client-side. Anything new that tooltips depend on must go through this payload. The same static tables are shared by the integrated server and client in singleplayer.
- **Hunger is neutralised, not removed**: `FoodDataMixin` cancels `tick`/`eat` and keeps food at 20, and `PlayerMixin` makes everything edible and grants Satiated in `Player.eat`. Cake bypasses `Player.eat`, so `CakeBlockMixin` handles it separately (key `minecraft:cake`). Vanilla `addEffect` merging is what gives "stronger Satiated wins".
- **Potion durations apply only to the drinkable `PotionItem`** (`PotionItemMixin`: drinking plus tooltip). Splash/lingering subclasses inherit `PotionItem`, so the tooltip redirect checks for `ThrowablePotionItem` and keeps vanilla durations for them.
- **Hunger/Saturation effects** are package-private vanilla classes, targeted by string name in `HungerMobEffectMixin` and `SaturationMobEffectMixin`. They drain/extend Satiated through `SatiatedEffect.drain`/`extend`, which use `forceAddEffect` so the update is safe to run while effects are ticking.
