# Changelog

All notable changes for the **Expanded Storage** Minecraft `1.21` Fabric port are documented in this file.

## [1.0.0+1.21] - 2026-11-26

### Target Environment & Toolchain
- **Minecraft:** `1.21`
- **Fabric Loader:** `0.20.1`
- **Yarn Mappings:** `1.21+build.9`
- **Fabric API:** `0.102.0+1.21`
- **Java Toolchain:** JDK `21` (`D:\Java\Java-21`, `JavaVersion.VERSION_21`, `options.release = 21`)
- **Gradle / Loom:** Gradle `8.14.3` with Fabric Loom `1.10-SNAPSHOT` (`1.10.5`)
- **Output Artifact:** Standardized JAR naming (`archives_base_name=expandedstorage-rk`, `archivesName = "${project.archives_base_name}-fabric-${project.minecraft_version}"`), producing `build/libs/expandedstorage-rk-fabric-1.21-1.0.0.jar`.

### Code, Identifier, Data Pack & Resource Migrations
- **Identifier Construction (`IdCompat`):**
  - Migrated `IdCompat#id(String)` from `new Identifier(MOD_ID, path)` to `Identifier.of(MOD_ID, path)`.
  - Migrated `IdCompat#mc(String)` from `new Identifier("minecraft", path)` to `Identifier.ofVanilla(path)`.
- **Data Packs & Singular Directory Migration (`src/main/generated/`):**
  - Cleaned legacy `1.20.6` plural data pack directories (`recipes/`, `loot_tables/`, `advancements/`, `tags/blocks/`) and executed `./gradlew runDatagen` for Minecraft `1.21`.
  - Regenerated all 25 data pack JSON files under the `1.21` singular folder hierarchy:
    - `src/main/generated/data/expandedstorage/recipe/*.json` (`result` key uses `"id"` and `"count": 1`)
    - `src/main/generated/data/expandedstorage/loot_table/blocks/*.json` (`minecraft:copy_components`)
    - `src/main/generated/data/expandedstorage/advancement/recipes/decorations/*.json`
    - `src/main/generated/data/minecraft/tags/block/*.json` (including `mineable/pickaxe.json`)
- **BlockEntity, Networking, Registry & Client GUI:**
  - Confirmed `ExpandedChestBlockEntity` (`readNbt` / `writeNbt` with `RegistryWrapper.WrapperLookup`, `getHeldStacks` / `setHeldStacks`), `StorageCompat`, and `NetworkCompat` (`ExtendedScreenHandlerType<T, ChestOpenData>` + `PacketCodec<RegistryByteBuf, ChestOpenData>`) compile and operate cleanly on `1.21`.
  - Confirmed `ModBlocks` (`AbstractBlock.Settings.create()`, `new Item.Settings()`), `ModBlockEntities` (`BlockEntityType.Builder.create(...).build()`), `ExpandedChestScreen`, `RenderCompat`, and `ExpandedChestBlockEntityRenderer` compile with zero deprecation warnings on `1.21`.
  - Validated `src/main/resources/expandedstorage.accesswidener` (`mutable field net/minecraft/screen/slot/Slot x I` and `y I`) under Loom `1.10.5` (`:validateAccessWidener`).
- **Documentation & Metadata:**
  - Updated `gradle.properties`, `build.gradle`, `fabric.mod.json` (expanded to `"minecraft": "~1.21"`, `"fabricloader": ">=0.20.1"`, `"java": ">=21"`, `"fabric-api": ">=0.102.0+1.21"`), `README.md`, `docs/DOCUMENTATION_EN.md`, and `docs/DOCUMENTATION_VN.md`.

### Build & Runtime Verification
- Executed `./gradlew runDatagen` and `./gradlew clean build` with zero compiler errors, zero deprecation warnings, and zero severe warnings (`BUILD SUCCESSFUL`).
- Executed `./gradlew runClient` on Minecraft `1.21` with JDK `21`, Fabric Loader `0.20.1`, and Fabric API `0.102.0+1.21`:
  - Confirmed `expandedstorage 1.0.0` loaded and logged `Expanded Storage initialized (6 chest tiers registered).`.
  - Confirmed `minecraft:textures/atlas/chest.png-atlas` and `minecraft:textures/atlas/gui.png-atlas` stitched cleanly, OpenAL audio initialized, and zero Mixin errors or mod-originated exceptions occurred.
