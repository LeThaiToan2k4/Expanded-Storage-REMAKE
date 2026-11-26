# Changelog

All notable changes for the **Expanded Storage** Minecraft `1.20.6` Fabric port are documented in this file.

## [1.0.0+1.20.6] - 2026-11-26

### Target Environment & Toolchain
- **Minecraft:** `1.20.6`
- **Fabric Loader:** `0.20.1`
- **Yarn Mappings:** `1.20.6+build.3`
- **Fabric API:** `0.100.8+1.20.6`
- **Java Toolchain:** JDK `21` (`D:\Java\Java-21`, `JavaVersion.VERSION_21`, `options.release = 21`)
- **Gradle / Loom:** Gradle `8.14.3` with Fabric Loom `1.10-SNAPSHOT` (`1.10.5`)
- **Output Artifact:** Standardized JAR naming (`archives_base_name=expandedstorage-rk`, `archivesName = "${project.archives_base_name}-fabric-${project.minecraft_version}"`), producing `build/libs/expandedstorage-rk-fabric-1.20.6-1.0.0.jar`.

### Code, BlockEntity, Networking, Registry, DataGen & Resource Alignment
- **Block & BlockEntity (`ExpandedChestBlock`, `ExpandedChestBlockEntity`, `StorageCompat`):**
  - Verified `ExpandedChestBlock` (`validateTicker`, `LootableInventory#generateLoot`), `ExpandedChestBlockEntity` (`readNbt` / `writeNbt` with `RegistryWrapper.WrapperLookup`, `LootableInventory#readLootTable` / `writeLootTable`, `setHeldStacks` / `getHeldStacks`), and `StorageCompat` (`Inventories.readNbt` / `writeNbt` with `WrapperLookup`) against Minecraft `1.20.6` and Yarn `1.20.6+build.3`.
- **ScreenHandler Networking (`NetworkCompat`):**
  - Confirmed `ExtendedScreenHandlerType<T, ChestOpenData>` and `ExtendedScreenHandlerFactory<ChestOpenData>#getScreenOpeningData` backed by `PacketCodec<RegistryByteBuf, ChestOpenData>` compile and operate cleanly with Fabric API `0.100.8+1.20.6`.
- **Block & BlockEntity Registration (`ModBlocks`, `ModBlockEntities`):**
  - Confirmed vanilla `AbstractBlock.Settings.create()`, `new Item.Settings()`, and `BlockEntityType.Builder.create(...).build()` compile with zero deprecation warnings on `1.20.6`.
- **Client GUI, Rendering & Access Widener:**
  - Validated `src/main/resources/expandedstorage.accesswidener` (`mutable field net/minecraft/screen/slot/Slot x I` and `y I`) under Loom `1.10.5` (`:validateAccessWidener`).
  - Confirmed `ExpandedChestScreen#render`, `RenderCompat`, and `ExpandedChestBlockEntityRenderer` compile and render cleanly on `1.20.6`.
- **DataGen & Generated Data Packs (`ModLootTableProvider`, `ModRecipeProvider`, `ModBlockTagProvider`, `src/main/generated/`):**
  - Executed `./gradlew runDatagen` against `1.20.6`, confirming all 25 generated recipe, advancement, loot table (`minecraft:copy_components`), and block tag JSON files under `src/main/generated/` match the `1.20.6` schema (`recipes/` plural folder and `{"count": 1, "id": "..."}` result format).
- **Documentation & Metadata:**
  - Updated `gradle.properties`, `build.gradle`, `fabric.mod.json` (expanded to `"minecraft": "~1.20.6"`, `"fabricloader": ">=0.20.1"`, `"java": ">=21"`, `"fabric-api": ">=0.100.8+1.20.6"`), `README.md`, `docs/DOCUMENTATION_EN.md`, and `docs/DOCUMENTATION_VN.md`.

### Build & Runtime Verification
- Executed `./gradlew runDatagen` and `./gradlew clean build` with zero compiler errors, zero deprecation warnings, and zero severe warnings (`BUILD SUCCESSFUL`).
- Executed `./gradlew runClient` on Minecraft `1.20.6` with JDK `21`, Fabric Loader `0.20.1`, and Fabric API `0.100.8+1.20.6`:
  - Confirmed `expandedstorage 1.0.0` loaded and logged `Expanded Storage initialized (6 chest tiers registered).`.
  - Confirmed `minecraft:textures/atlas/chest.png-atlas` and `minecraft:textures/atlas/gui.png-atlas` stitched cleanly, OpenAL audio initialized, and zero Mixin errors or mod-originated exceptions occurred.
