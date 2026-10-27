# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Satiated is a small Fabric mod for Minecraft 1.21.1 (mod id `satiated`, package `maihatsuu.satiated`): some foods grant bonus effects, drinkable beneficial potions last longer, and soups, stews and potions stack to 16. Everything else is vanilla. Earlier, larger designs (a hunger replacement with a healing queue, then tunable exhaustion) are in git history; the healing-queue version is on the `healing-queue` branch. `fabric.mod.json` still has the template's authors and contact links.

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
  - `src/client/`: client-only code. Entrypoint `client.SatiatedClient` (`ClientModInitializer`) holds the bonus-effect tooltip. It currently has no mixins (the client mixin config is empty). Client code can reference main, but main must never reference client classes.
- **Mixins**: there are two configs, and each new mixin class has to be registered in the right one:
  - `src/main/resources/satiated.mixins.json` (package `maihatsuu.satiated.mixin`, `mixins` array)
  - `src/client/resources/satiated.client.mixins.json` (package `maihatsuu.satiated.client.mixin`, `client` array)
  - Both set `defaultRequire: 1`, so an injector that fails to match its target crashes at startup instead of being silently skipped.
- **Assets/data** go under `src/main/resources/assets/satiated/` and `data/satiated/`. `fabric.mod.json` has `${version}` substituted from `gradle.properties` during `processResources`.
- **`./gradlew runServer`** stops at the EULA prompt until `run/eula.txt` is set to `true`. Most mixin targets only load once a world starts, so a passing `build` doesn't prove the injectors match.

## How the mod fits together

Nothing is data-driven; all values are constants in code.

- **Food bonus effects** (`food/SatiatedFoods`): a map of item → effects. `PlayerMixin` applies them at the start of `Player.eat`; cake bypasses `Player.eat`, so `CakeBlockMixin` applies cake's per slice. `apply` adds copies because `addEffect` keeps the instance it's given. The client tooltip reads the same map.
- **Potion durations** (`potion/PotionDurations`): vanilla (`minecraft:`) potions with any beneficial effect get 5 min, `long_` variants 10 min and `strong_` variants 2.5 min. Instant and custom effects are untouched. `PotionItemMixin` redirects both drinking and the tooltip. Splash/lingering subclasses inherit `PotionItem`, so the tooltip redirect checks for `ThrowablePotionItem` and keeps vanilla durations for them.
- **Stacking** (`ModItemComponents`): sets `MAX_STACK_SIZE` to 16 via `DefaultItemComponentEvents.MODIFY`. It must be installed on both sides, or inventories desync.
