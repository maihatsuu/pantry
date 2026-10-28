# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Pantry (formerly Satiated) is a small Fabric mod for Minecraft 1.21.1 (mod id `pantry`, package `maihatsuu.pantry`): some foods grant bonus effects, and soups, stews and potions stack to 16. Everything else is vanilla. Earlier, larger designs (a hunger replacement with a healing queue, then tunable exhaustion) are in git history; the healing-queue version is on the `healing-queue` branch. Longer potion durations (5 min, 10 with redstone) live on the `extended-potions` branch.

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
  - `src/client/`: client-only code. Entrypoint `client.PantryClient` (`ClientModInitializer`) holds the bonus-effect tooltip. Client code can reference main, but main must never reference client classes.
- **Mixins** are registered in `src/main/resources/pantry.mixins.json` (package `maihatsuu.pantry.mixin`). It sets `defaultRequire: 1`, so an injector that fails to match its target crashes at startup instead of being silently skipped. There's no client mixin config; if a client-only mixin is needed, add `pantry.client.mixins.json` under `src/client/resources/` and list it in `fabric.mod.json` with `"environment": "client"`.
- **Assets/data** go under `src/main/resources/assets/pantry/` and `data/pantry/` (there's no data folder at the moment). `fabric.mod.json` has `${version}` substituted from `gradle.properties` during `processResources`.
- **`./gradlew runServer`** stops at the EULA prompt until `run/eula.txt` is set to `true`. Most mixin targets only load once a world starts, so a passing `build` doesn't prove the injectors match.

## How the mod fits together

Nothing is data-driven; all values are constants in code.

- **Food bonus effects** (`food/FoodBonuses`): a map of item → effects. `PlayerMixin` applies them at the start of `Player.eat` (cake bypasses `Player.eat`, so it can't have a bonus without a `CakeBlock` mixin). `apply` adds copies because `addEffect` keeps the instance it's given, and applies instant effects immediately the way potions do. The client tooltip reads the same map.
- **Stacking** (`ModItemComponents`): sets `MAX_STACK_SIZE` to 16 via `DefaultItemComponentEvents.MODIFY`. It must be installed on both sides, or inventories desync.
