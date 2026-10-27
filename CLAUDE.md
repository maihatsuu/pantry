# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Satiated is a Fabric mod for Minecraft 1.21.1 (mod id `satiated`, package `maihatsuu.satiated`). It keeps vanilla's hunger bar and regeneration but makes every exhaustion source tunable, adds per-food bonus effects, gives drinkable potions longer durations from a table, and stacks soups, stews and potions to 16. An earlier design that replaced hunger with a visible healing queue is preserved on the `healing-queue` branch. `fabric.mod.json` still has the template's authors and contact links.

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
  - `src/client/`: client-only code. Entrypoint `client.SatiatedClient` (`ClientModInitializer`) holds the sync receiver and the bonus-effect tooltip. It currently has no mixins (the client mixin config is empty). Client code can reference main, but main must never reference client classes.
- **Mixins**: there are two configs, and each new mixin class has to be registered in the right one:
  - `src/main/resources/satiated.mixins.json` (package `maihatsuu.satiated.mixin`, `mixins` array)
  - `src/client/resources/satiated.client.mixins.json` (package `maihatsuu.satiated.client.mixin`, `client` array)
  - Both set `defaultRequire: 1`, so an injector that fails to match its target crashes at startup instead of being silently skipped.
- **Assets/data** go under `src/main/resources/assets/satiated/` and `data/satiated/`. `fabric.mod.json` has `${version}` substituted from `gradle.properties` during `processResources`.
- **`./gradlew runServer`** stops at the EULA prompt until `run/eula.txt` is set to `true`. Most mixin targets only load once a world starts, so a passing `build` doesn't prove the injectors match.

## How the mod fits together

- **Balance lives in datapack JSON, not code.** Three files, all loaded through the generic `data/MergedJsonTable`, which merges every datapack's copy in pack order (`"replace": true` clears earlier packs):
  - `data/satiated/exhaustion.json`: source → cost.
  - `data/satiated/foods.json`: item id → bonus `effects` in vanilla `FoodProperties.PossibleEffect` shape, `replace_vanilla_effects`.
  - `data/satiated/potion_durations.json`: potion id → drinkable duration in ticks.
- **`foods.json` and `exhaustion.json` are generated; don't hand-edit them.** The source of truth is the CSVs in `balance/`. After changing them, run `python3 balance/generate_balance.py`, which validates every row and writes nothing if any row fails.
- **Exhaustion** (`exhaustion/ExhaustionCosts`): each vanilla `causeFoodExhaustion`/`addExhaustion` call site has a `@ModifyArg`, in `ServerPlayerExhaustionMixin` (movement), `PlayerMixin` (jump, attack, damage), `BlockMixin` (mining), `FoodDataMixin` (natural regen) and `HungerMobEffectMixin`.
  - `ExhaustionCosts.scale` rescales vanilla's argument by `cost / vanillaCost`, which keeps vanilla's distance, level and damage-type scaling.
  - `walk` and `crouch` cost 0 in vanilla, so their hooks recompute the distance from `checkMovementStatistics`'s own arguments instead.
  - Movement and jump hooks depend on call **ordinals**, which are documented in the mixins. Re-check them against decompiled sources if Minecraft is updated.
  - Table keys are read as `ResourceLocation`s, so `"sprint"` is `minecraft:sprint`.
  - The table isn't synced, since exhaustion is server-authoritative.
- **Food bonus effects** (`food/SatiatedFoods`): `PlayerMixin` applies them in `Player.eat` and can strip a food's vanilla effects. Cake bypasses `Player.eat`, so `CakeBlockMixin` handles it (key `minecraft:cake`).
- **Tables are synced to clients** by `network/SyncTablesPayload` on join and after `/reload`, because the food and potion tooltips read them client-side. Anything new that tooltips depend on must go through this payload. The same static tables are shared by the integrated server and client in singleplayer.
- **Potion durations apply only to the drinkable `PotionItem`** (`PotionItemMixin`: drinking plus tooltip). Splash/lingering subclasses inherit `PotionItem`, so the tooltip redirect checks for `ThrowablePotionItem` and keeps vanilla durations for them.
