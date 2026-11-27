# Changelog

All notable changes for the **Expanded Storage** Minecraft `1.21.6` Fabric port are documented in this file.

## [1.0.0+1.21.6] - 2026-11-27

### Target Environment & Toolchain
- **Minecraft:** `1.21.6`
- **Fabric Loader:** `0.20.1`
- **Yarn Mappings:** `1.21.6+build.1`
- **Fabric API:** `0.128.2+1.21.6`
- **Java Toolchain:** JDK `21` (`D:\Java\Java-21`, `JavaVersion.VERSION_21`, `options.release = 21`)
- **Gradle / Loom:** Gradle `8.14.3` with Fabric Loom `1.10-SNAPSHOT` (`1.10.5`)
- **Output Artifact:** Standardized JAR naming (`archives_base_name=expandedstorage-rk`, `archivesName = "${project.archives_base_name}-fabric-${project.minecraft_version}"`), producing `build/libs/expandedstorage-rk-fabric-1.21.6-1.0.0.jar`.

### Code, BlockEntity Data Views, GUI RenderPipeline, DataGen & Resource Migration
- **BlockEntity Data View Persistence (`StorageCompat`, `ExpandedChestBlockEntity`):**
  - Migrated `StorageCompat` from `Inventories.readNbt` / `Inventories.writeNbt` to the Minecraft `1.21.6` `ReadView` / `WriteView` storage API (`Inventories.readData(ReadView, DefaultedList<ItemStack>)` and `Inventories.writeData(WriteView, DefaultedList<ItemStack>)`).
  - Migrated `ExpandedChestBlockEntity` from `readNbt` / `writeNbt` to `readData(ReadView view)` and `writeData(WriteView view)`, passing `ReadView` and `WriteView` directly to `super.readData` / `super.writeData`, `readLootTable` / `writeLootTable`, and `StorageCompat`.
- **GUI RenderPipeline & ARGB Text Colors (`RenderCompat`):**
  - Updated `RenderCompat.drawRegion` from `RenderLayer::getGuiTextured` to `RenderPipelines.GUI_TEXTURED` (`com.mojang.blaze3d.pipeline.RenderPipeline`) matching `DrawContext#drawTexture` in Minecraft `1.21.6`.
  - Updated `RenderCompat.titleColor` and `RenderCompat.tabTextColor` to full opaque ARGB values (`0xFFE6E9EF`, `0xFF404040`, `0xFFFFF3C4`, `0xFF9CA3AF`, `0xFF2B2B2B`, `0xFF585858`) required by `1.21.6` `DrawContext#drawText`.
- **DataGen Block Tag Provider (`ModBlockTagProvider`):**
  - Updated `ModBlockTagProvider.configure` from `getOrCreateTagBuilder(...)` to `valueLookupBuilder(...)` for `FabricTagProvider.BlockTagProvider` in Fabric API `0.128.2+1.21.6`.
  - Executed `./gradlew runDatagen` against Minecraft `1.21.6`, confirming all 25 generated data pack JSON files (`recipe/`, `loot_table/blocks/`, `advancement/recipes/decorations/`, `tags/block/`) match the `1.21.6` schema.
- **Documentation & Metadata:**
  - Updated `gradle.properties`, `fabric.mod.json` (expanded to `"minecraft": "~1.21.6"`, `"fabricloader": ">=0.20.1"`, `"java": ">=21"`, `"fabric-api": ">=0.128.2+1.21.6"`), `README.md`, `docs/DOCUMENTATION_EN.md`, and `docs/DOCUMENTATION_VN.md`.

### Build & Runtime Verification
- Executed `./gradlew runDatagen` and `./gradlew clean build --refresh-dependencies` with zero compiler errors, zero deprecation warnings, and zero severe warnings (`BUILD SUCCESSFUL`).
- Executed `./gradlew runClient` on Minecraft `1.21.6` with JDK `21`, Fabric Loader `0.20.1`, and Fabric API `0.128.2+1.21.6`:
  - Confirmed `expandedstorage 1.0.0` loaded and logged `Expanded Storage initialized (6 chest tiers registered).`.
  - Confirmed all 6 `assets/expandedstorage/items/*_chest.json` special item models baked cleanly without warnings, `minecraft:textures/atlas/chest.png-atlas` (`512x256x3`) and `minecraft:textures/atlas/gui.png-atlas` (`1024x512x0`) stitched cleanly, OpenAL audio initialized, and zero Mixin errors or mod-originated exceptions occurred.
