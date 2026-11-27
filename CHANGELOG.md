# Changelog

All notable changes for the **Expanded Storage** Minecraft `1.21.2` Fabric port are documented in this file.

## [1.0.0+1.21.2] - 2026-11-27

### Target Environment & Toolchain
- **Minecraft:** `1.21.2`
- **Fabric Loader:** `0.20.1`
- **Yarn Mappings:** `1.21.2+build.1`
- **Fabric API:** `0.106.1+1.21.2`
- **Java Toolchain:** JDK `21` (`D:\Java\Java-21`, `JavaVersion.VERSION_21`, `options.release = 21`)
- **Gradle / Loom:** Gradle `8.14.3` with Fabric Loom `1.10-SNAPSHOT` (`1.10.5`)
- **Output Artifact:** Standardized JAR naming (`archives_base_name=expandedstorage-rk`, `archivesName = "${project.archives_base_name}-fabric-${project.minecraft_version}"`), producing `build/libs/expandedstorage-rk-fabric-1.21.2-1.0.0.jar`.

### Code, BlockEntity, Registry, GUI, DataGen & Resource Migration
- **Block & Item Registration (`ModBlocks`):**
  - Updated `ModBlocks.registerChest` to construct explicit `RegistryKey<Block>` (`RegistryKeys.BLOCK`) and `RegistryKey<Item>` (`RegistryKeys.ITEM`) keys before instantiation.
  - Applied `.registryKey(blockKey)` on `AbstractBlock.Settings` and `.registryKey(itemKey).useBlockPrefixedTranslationKey()` on `Item.Settings` as required by Minecraft `1.21.2` registry-keyed settings and block-prefixed item translation keys.
- **Chest Block Constructor (`ExpandedChestBlock`):**
  - Updated `ExpandedChestBlock` to invoke `super(entityTypeSupplier, settings)` matching the flipped `ChestBlock(Supplier<BlockEntityType<? extends ChestBlockEntity>>, AbstractBlock.Settings)` constructor parameter order in Minecraft `1.21.2`.
- **BlockEntityType Builder Migration (`ModBlockEntities`):**
  - Migrated `ModBlockEntities` from the removed vanilla `BlockEntityType.Builder.create(...)` to Fabric API's `FabricBlockEntityTypeBuilder.create(...).build()`.
- **GUI Texture Rendering (`RenderCompat`):**
  - Updated `RenderCompat.drawRegion` to call `ctx.drawTexture(RenderLayer::getGuiTextured, tex, x, y, (float) u, (float) v, width, height, TEX_SIZE, TEX_SIZE)` matching the `1.21.2` `DrawContext#drawTexture` signature requiring a `Function<Identifier, RenderLayer>`.
- **Data Generation & Recipe Schema (`ModRecipeProvider`, `src/main/generated/`):**
  - Updated `ModRecipeProvider` to implement `FabricRecipeProvider#getRecipeGenerator(RegistryWrapper.WrapperLookup, RecipeExporter)` and `getName()`, using `RecipeGenerator#createShaped` and `RegistryKey<Recipe<?>>` (`RegistryKeys.RECIPE`) targets for `offerTo`.
  - Executed `./gradlew runDatagen` to regenerate all recipe and advancement JSON files under `src/main/generated/data/expandedstorage/` using the `1.21.2` string ingredient schema (`"C": "minecraft:chest"`, `"M": "minecraft:copper_ingot"`).
- **Documentation & Metadata:**
  - Updated `gradle.properties`, `fabric.mod.json` (expanded to `"minecraft": "~1.21.2"`, `"fabricloader": ">=0.20.1"`, `"java": ">=21"`, `"fabric-api": ">=0.106.1+1.21.2"`), `README.md`, `docs/DOCUMENTATION_EN.md`, and `docs/DOCUMENTATION_VN.md`.

### Build & Runtime Verification
- Executed `./gradlew runDatagen` and `./gradlew clean build` with zero compiler errors, zero deprecation warnings, and zero severe warnings (`BUILD SUCCESSFUL`).
- Executed `./gradlew runClient` on Minecraft `1.21.2` with JDK `21`, Fabric Loader `0.20.1`, and Fabric API `0.106.1+1.21.2`:
  - Confirmed `expandedstorage 1.0.0` loaded and logged `Expanded Storage initialized (6 chest tiers registered).`.
  - Confirmed `minecraft:textures/atlas/chest.png-atlas` (`512x256x3`) and `minecraft:textures/atlas/gui.png-atlas` (`1024x512x0`) stitched cleanly, OpenAL audio initialized, and zero Mixin errors or mod-originated exceptions occurred.
