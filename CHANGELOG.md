# Changelog

All notable changes for the **Expanded Storage** Minecraft `1.21.4` Fabric port are documented in this file.

## [1.0.0+1.21.4] - 2026-11-27

### Target Environment & Toolchain
- **Minecraft:** `1.21.4`
- **Fabric Loader:** `0.20.1`
- **Yarn Mappings:** `1.21.4+build.8`
- **Fabric API:** `0.119.4+1.21.4`
- **Java Toolchain:** JDK `21` (`D:\Java\Java-21`, `JavaVersion.VERSION_21`, `options.release = 21`)
- **Gradle / Loom:** Gradle `8.14.3` with Fabric Loom `1.10-SNAPSHOT` (`1.10.5`)
- **Output Artifact:** Standardized JAR naming (`archives_base_name=expandedstorage-rk`, `archivesName = "${project.archives_base_name}-fabric-${project.minecraft_version}"`), producing `build/libs/expandedstorage-rk-fabric-1.21.4-1.0.0.jar`.

### Code, BlockEntity, Item Models, GUI, DataGen & Resource Migration
- **1.21.4 Client Item Model Definitions & 3D Chest Item Rendering (`assets/expandedstorage/items/*.json`, `ExpandedStorageClient`):**
  - Added the six `1.21.4` client item model definition files under `src/main/resources/assets/expandedstorage/items/` (`copper_chest.json`, `iron_chest.json`, `gold_chest.json`, `diamond_chest.json`, `obsidian_chest.json`, `netherite_chest.json`).
  - Configured each item definition to use `"type": "minecraft:special"` with `"base": "expandedstorage:item/<tier>_chest"` and `"model": { "type": "minecraft:chest", "texture": "expandedstorage:<tier>/normal" }`, delegating 3D item chest rendering natively to vanilla's `ChestModelRenderer` and `TexturedRenderLayers.createChestTextureId`.
  - Removed the obsolete `BuiltinItemRendererRegistry` registration and dummy render entities from `ExpandedStorageClient`.
- **BlockEntity Renderer Direction Rotation (`ExpandedChestBlockEntityRenderer`):**
  - Updated `ExpandedChestBlockEntityRenderer.render` from the removed `Direction#asRotation()` to `Direction#getPositiveHorizontalDegrees()` matching Yarn `1.21.4+build.8`.
- **DataGen Package Relocation (`ModRecipeProvider`):**
  - Updated `ModRecipeProvider` imports from `net.minecraft.data.server.recipe.*` to the relocated `1.21.4` package `net.minecraft.data.recipe.*` (`RecipeExporter`, `RecipeGenerator`, `SmithingTransformRecipeJsonBuilder`).
  - Executed `./gradlew runDatagen` against Minecraft `1.21.4`, confirming all 25 generated data pack JSON files (`recipe/`, `loot_table/blocks/`, `advancement/recipes/decorations/`, `tags/block/`) match the `1.21.4` schema.
- **Registry, BlockEntity & GUI Compatibility (`ModBlocks`, `ModBlockEntities`, `ExpandedChestBlock`, `RenderCompat`, `NetworkCompat`):**
  - Confirmed registry-keyed block and item settings (`ModBlocks`), `FabricBlockEntityTypeBuilder` (`ModBlockEntities`), `DrawContext#drawTexture(RenderLayer::getGuiTextured, ...)` (`RenderCompat`), and `ExtendedScreenHandlerType` (`NetworkCompat`) compile cleanly with zero deprecation warnings on `1.21.4`.
- **Documentation & Metadata:**
  - Updated `gradle.properties`, `fabric.mod.json` (expanded to `"minecraft": "~1.21.4"`, `"fabricloader": ">=0.20.1"`, `"java": ">=21"`, `"fabric-api": ">=0.119.4+1.21.4"`), `README.md`, `docs/DOCUMENTATION_EN.md`, and `docs/DOCUMENTATION_VN.md`.

### Build & Runtime Verification
- Executed `./gradlew runDatagen` and `./gradlew clean build --refresh-dependencies` with zero compiler errors, zero deprecation warnings, and zero severe warnings (`BUILD SUCCESSFUL`).
- Executed `./gradlew runClient` on Minecraft `1.21.4` with JDK `21`, Fabric Loader `0.20.1`, and Fabric API `0.119.4+1.21.4`:
  - Confirmed `expandedstorage 1.0.0` loaded and logged `Expanded Storage initialized (6 chest tiers registered).`.
  - Confirmed all 6 `assets/expandedstorage/items/*_chest.json` special item models baked cleanly without warnings, `minecraft:textures/atlas/chest.png-atlas` (`512x256x3`) and `minecraft:textures/atlas/gui.png-atlas` (`1024x512x0`) stitched cleanly, OpenAL audio initialized, and zero Mixin errors or mod-originated exceptions occurred.
