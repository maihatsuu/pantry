# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Pantry (formerly Satiated) is a small Fabric mod for Minecraft 1.21.1 (mod id `pantry`, package `maihatsuu.pantry`): some foods grant bonus effects, all foods can be eaten when full, small snacks eat twice as fast, drinkable potions last longer, soups, stews, potions and cakes stack to 16. Everything else is vanilla. Earlier, larger designs (a hunger replacement with a healing queue, then tunable exhaustion) are in git history; the healing-queue version is on the `healing-queue` branch. Every feature has a config toggle; longer potions is the only one off by default.

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
  - `src/client/`: client-only code. It holds the Mod Menu config screen and `PantryClient`, the client entrypoint that adds food effect tooltips (see below). Client code can reference main, but main must never reference client classes.
- **Mixins** are registered in `src/main/resources/pantry.mixins.json` (package `maihatsuu.pantry.mixin`). It sets `defaultRequire: 1`, so an injector that fails to match its target crashes at startup instead of being silently skipped. There's no client mixin config; if a client-only mixin is needed, add `pantry.client.mixins.json` under `src/client/resources/` and list it in `fabric.mod.json` with `"environment": "client"`.
- **Assets/data** go under `src/main/resources/assets/pantry/` and `data/pantry/` (there's no data folder at the moment). `fabric.mod.json` has `${version}` substituted from `gradle.properties` during `processResources`.
- **`./gradlew runServer`** stops at the EULA prompt until `run/eula.txt` is set to `true`. Most mixin targets only load once a world starts, so a passing `build` doesn't prove the injectors match.

## How the mod fits together

Nothing is data-driven; all values are constants in code. Each feature can be switched off, though:

- **Config** (`PantryConfig`): seven boolean toggles, all on by default except `longerPotions`, saved as JSON (Gson) in `config/pantry.json`. Each feature checks its toggle. `ModItemComponents` reads it once at startup, so eat when full, fast snacks, bigger stacks and the Golden Carrot nerf need a restart and must match between client and server. Food bonuses and longer potions are read live, on the server for gameplay (the potion tooltip uses the client's config).
- **Config screen** (`src/client/.../client/`): `ModMenuIntegration` is the `modmenu` entrypoint, and `PantryConfigScreen` builds it with Cloth Config. Both mods are optional (`modCompileOnly` + `modLocalRuntime`, `suggests` in `fabric.mod.json`), so Cloth classes live only in `PantryConfigScreen`, which is reached only after checking that `cloth-config` is loaded. Labels are in `assets/pantry/lang/en_us.json`.

- **Food bonus effects** (`food/FoodBonuses`): a map of item → effects. `PlayerMixin` applies them at the start of `Player.eat` (cake bypasses `Player.eat`, so it can't have a bonus without hooking `CakeBlock.eat`). `apply` adds copies because `addEffect` keeps the instance it's given. Bonuses are timed effects only; an instant one (like Instant Health) would need `applyInstantenousEffect` instead of `addEffect`. `PantryClient` lists a food's vanilla and bonus effects in its tooltip (`foodEffectTooltips`, read live on the client), skipping `HARMFUL` effects. Suspicious stew keeps its effects outside the `FOOD` component, so it shows nothing.
- **Potion durations** (`potion/PotionDurations`, off by default): the `EXTENDED` vanilla (`minecraft:`) potions (Swiftness, Strength, Leaping, Night Vision, Invisibility, Fire Resistance, Water Breathing) get 4 min (including `strong_` variants) and `long_` variants 8 min. Other potions, instant and custom effects are untouched. `PotionItemMixin` redirects both drinking and the tooltip. Splash/lingering subclasses inherit `PotionItem`, so the tooltip redirect checks for `ThrowablePotionItem` and keeps vanilla durations for them.
- **Stacking and always-edible** (`ModItemComponents`): via `DefaultItemComponentEvents.MODIFY`, sets `MAX_STACK_SIZE` (16 for potions, `SOUPS` and cake) and rebuilds every item's `FOOD` component with `canAlwaysEat` true, a 0.8s eat time for `FAST_FOODS`, and 8.0 saturation for Golden Carrot. Cake has no `FOOD` component, so `CakeBlockMixin` makes its own hunger check (`Player.canEat(false)`) always pass. It must be installed on both sides, or inventories desync and the client refuses to start eating when full.
