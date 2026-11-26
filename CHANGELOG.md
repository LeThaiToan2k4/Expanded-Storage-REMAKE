# Changelog

All notable changes for the **Expanded Storage** Minecraft `1.21.1` Fabric port are documented in this file.

## [1.0.0+1.21.1] - 2026-11-26

### Target Environment & Toolchain
- **Minecraft:** `1.21.1`
- **Fabric Loader:** `0.20.1`
- **Yarn Mappings:** `1.21.1+build.3`
- **Fabric API:** `0.116.18+1.21.1`
- **Java Toolchain:** JDK `21` (`D:\Java\Java-21`, `JavaVersion.VERSION_21`, `options.release = 21`)
- **Gradle / Loom:** Gradle `8.14.3` with Fabric Loom `1.10-SNAPSHOT` (`1.10.5`)
- **Output Artifact:** Standardized JAR naming (`archives_base_name=expandedstorage-rk`, `archivesName = "${project.archives_base_name}-fabric-${project.minecraft_version}"`), producing `build/libs/expandedstorage-rk-fabric-1.21.1-1.0.0.jar`.

### Code, BlockEntity, Networking, GUI, DataGen & Resource Alignment
- **Identifier & Compat Adapters (`IdCompat`, `StorageCompat`, `NetworkCompat`, `RenderCompat`):**
  - Verified `IdCompat` (`Identifier.of`, `Identifier.ofVanilla`), `StorageCompat` (`Inventories.readNbt` / `writeNbt` with `RegistryWrapper.WrapperLookup`), `NetworkCompat` (`ExtendedScreenHandlerType<T, ChestOpenData>` + `PacketCodec<RegistryByteBuf, ChestOpenData>`), and `RenderCompat` (`DrawContext#drawTexture`, `drawText`, `drawTooltip`) compile and operate cleanly against Minecraft `1.21.1` and Fabric API `0.116.18+1.21.1`.
- **Chest Block, BlockEntity & Registry (`ExpandedChestBlock`, `ExpandedChestBlockEntity`, `ModBlocks`, `ModBlockEntities`):**
  - Confirmed `ExpandedChestBlock` (`validateTicker`, `LootableInventory#generateLoot`), `ExpandedChestBlockEntity` (`readNbt` / `writeNbt` with `WrapperLookup`, `getHeldStacks` / `setHeldStacks`), `ModBlocks` (`AbstractBlock.Settings.create()`, `new Item.Settings()`), and `ModBlockEntities` (`BlockEntityType.Builder.create(...).build()`) compile with zero deprecation warnings on `1.21.1`.
- **Access Widener & DataGen (`expandedstorage.accesswidener`, `src/main/generated/`):**
  - Validated `src/main/resources/expandedstorage.accesswidener` (`mutable field net/minecraft/screen/slot/Slot x I` and `y I`) under Loom `1.10.5` (`:validateAccessWidener`).
  - Executed `./gradlew runDatagen` against `1.21.1`, confirming all 25 generated data pack JSON files under the singular directory layout (`recipe/`, `loot_table/blocks/`, `advancement/recipes/decorations/`, `tags/block/`) match the `1.21.1` schema.
- **Documentation & Metadata:**
  - Updated `gradle.properties`, `build.gradle`, `fabric.mod.json` (expanded to `"minecraft": "~1.21.1"`, `"fabricloader": ">=0.20.1"`, `"java": ">=21"`, `"fabric-api": ">=0.116.18+1.21.1"`), `README.md`, `docs/DOCUMENTATION_EN.md`, and `docs/DOCUMENTATION_VN.md`.

### Build & Runtime Verification
- Executed `./gradlew runDatagen` and `./gradlew clean build` with zero compiler errors, zero deprecation warnings, and zero severe warnings (`BUILD SUCCESSFUL`).
- Executed `./gradlew runClient` on Minecraft `1.21.1` with JDK `21`, Fabric Loader `0.20.1`, and Fabric API `0.116.18+1.21.1`:
  - Confirmed `expandedstorage 1.0.0` loaded and logged `Expanded Storage initialized (6 chest tiers registered).`.
  - Confirmed `minecraft:textures/atlas/chest.png-atlas` and `minecraft:textures/atlas/gui.png-atlas` stitched cleanly, OpenAL audio initialized, and zero Mixin errors or mod-originated exceptions occurred.
