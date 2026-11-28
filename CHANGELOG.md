# Changelog

All notable changes for the **Expanded Storage** Minecraft `1.21.8` Fabric port are documented in this file.

## [1.0.0+1.21.8] - 2026-11-28

### Target Environment & Toolchain
- **Minecraft:** `1.21.8`
- **Fabric Loader:** `0.20.1`
- **Yarn Mappings:** `1.21.8+build.1`
- **Fabric API:** `0.136.1+1.21.8`
- **Java Toolchain:** JDK `21` (`D:\Java\Java-21`, `JavaVersion.VERSION_21`, `options.release = 21`)
- **Gradle / Loom:** Gradle `8.14.3` with Fabric Loom `1.10-SNAPSHOT` (`1.10.5`)
- **Output Artifact:** Standardized JAR naming (`archives_base_name=expandedstorage-rk`, `archivesName = "${project.archives_base_name}-fabric-${project.minecraft_version}"`), producing `build/libs/expandedstorage-rk-fabric-1.21.8-1.0.0.jar`.

### Code, BlockEntity Data Views, GUI RenderPipeline, DataGen & Resource Alignment
- **BlockEntity Data View Persistence (`StorageCompat`, `ExpandedChestBlockEntity`):**
  - Verified full binary and mapping compatibility of the `ReadView` / `WriteView` storage API (`Inventories.readData(ReadView, DefaultedList<ItemStack>)` and `Inventories.writeData(WriteView, DefaultedList<ItemStack>)`) and `ExpandedChestBlockEntity#readData(ReadView)` / `writeData(WriteView)` with Minecraft `1.21.8` (`1.21.8+build.1`).
- **GUI RenderPipeline & ARGB Text Colors (`RenderCompat`, `ExpandedChestScreen`):**
  - Verified `RenderPipelines.GUI_TEXTURED` (`DrawContext#drawTexture`) and opaque ARGB text colors (`0xFFE6E9EF`, `0xFF404040`, `0xFFFFF3C4`, `0xFF9CA3AF`, `0xFF2B2B2B`, `0xFF585858`) across the dynamic 9-slice T-shape renderer (`ADAPTIVE`, `SPLIT_WING`, and `SMART_TABBED` modes) in Minecraft `1.21.8`.
- **DataGen & Data Pack Schema (`ModRecipeProvider`, `ModLootTableProvider`, `ModBlockTagProvider`):**
  - Executed `./gradlew runDatagen` against Minecraft `1.21.8` and Fabric API `0.136.1+1.21.8`, validating all 25 generated data pack JSON files (`recipe/`, `loot_table/blocks/`, `advancement/recipes/decorations/`, and `tags/block/` via `valueLookupBuilder`).
- **Documentation & Metadata:**
  - Updated `gradle.properties`, `fabric.mod.json` (expanded to `"minecraft": "~1.21.8"`, `"fabricloader": ">=0.20.1"`, `"java": ">=21"`, `"fabric-api": ">=0.136.1+1.21.8"`), `README.md`, `docs/DOCUMENTATION_EN.md`, and `docs/DOCUMENTATION_VN.md`.

### Build & Runtime Verification
- Executed `./gradlew clean build --refresh-dependencies` and `./gradlew runDatagen build` with zero compiler errors, zero deprecation warnings, and zero severe warnings (`BUILD SUCCESSFUL`).
- Executed `./gradlew runClient` on Minecraft `1.21.8` with JDK `21`, Fabric Loader `0.20.1`, and Fabric API `0.136.1+1.21.8`:
  - Confirmed `expandedstorage 1.0.0` loaded and logged `Expanded Storage initialized (6 chest tiers registered).`.
  - Confirmed all 6 `assets/expandedstorage/items/*_chest.json` special item models baked cleanly without warnings, `minecraft:textures/atlas/chest.png-atlas` (`512x256x3`) and `minecraft:textures/atlas/gui.png-atlas` (`1024x512x0`) stitched cleanly, OpenAL audio initialized, and zero Mixin errors or mod-originated exceptions occurred.
