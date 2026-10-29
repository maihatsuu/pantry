# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Pantry (formerly Satiated) is a small Fabric mod for Minecraft 1.21.1 (mod id `pantry`, package `maihatsuu.pantry`): some foods grant bonus effects, all foods can be eaten when full, small snacks eat twice as fast, soups, stews and potions stack to 16, and cakes stack to 64. Everything else is vanilla. Earlier, larger designs (a hunger replacement with a healing queue, then tunable exhaustion) are in git history; the healing-queue version is on the `healing-queue` branch. Longer potion durations (5 min, 10 with redstone) live on the `extended-potions` branch.

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
  - `src/main/`: common/server-safe code. Entrypoint `Pantry` (`ModInitializer`) registers everything and defines `MOD_ID` and `LOGGER`.
  - `src/client/`: client-only code. It's empty at the moment, with no client entrypoint; if one is needed, add a `ClientModInitializer` and list it under `entrypoints.client` in `fabric.mod.json`. Client code can reference main, but main must never reference client classes.
- **Mixins** are registered in `src/main/resources/pantry.mixins.json` (package `maihatsuu.pantry.mixin`). It sets `defaultRequire: 1`, so an injector that fails to match its target crashes at startup instead of being silently skipped. There's no client mixin config; if a client-only mixin is needed, add `pantry.client.mixins.json` under `src/client/resources/` and list it in `fabric.mod.json` with `"environment": "client"`.
- **Assets/data** go under `src/main/resources/assets/pantry/` and `data/pantry/` (there's no data folder at the moment). `fabric.mod.json` has `${version}` substituted from `gradle.properties` during `processResources`.
- **`./gradlew runServer`** stops at the EULA prompt until `run/eula.txt` is set to `true`. Most mixin targets only load once a world starts, so a passing `build` doesn't prove the injectors match.

## How the mod fits together

Nothing is data-driven; all values are constants in code.

- **Food bonus effects** (`food/FoodBonuses`): a map of item → effects. `PlayerMixin` applies them at the start of `Player.eat` (cake bypasses `Player.eat`, so it can't have a bonus without hooking `CakeBlock.eat`). `apply` adds copies because `addEffect` keeps the instance it's given. Bonuses are timed effects only; an instant one (like Instant Health) would need `applyInstantenousEffect` instead of `addEffect`. There's no tooltip for them, matching vanilla foods, which don't show their effects either.
- **Stacking and always-edible** (`ModItemComponents`): via `DefaultItemComponentEvents.MODIFY`, sets `MAX_STACK_SIZE` (16 for potions and `SOUPS`, 64 for cake) and rebuilds every item's `FOOD` component with `canAlwaysEat` true, a 0.8s eat time for `FAST_FOODS`, and 8.0 saturation for Golden Carrot. Cake has no `FOOD` component, so `CakeBlockMixin` makes its own hunger check (`Player.canEat(false)`) always pass. It must be installed on both sides, or inventories desync and the client refuses to start eating when full.
