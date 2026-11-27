# Changelog

All notable changes for the **Expanded Storage** Minecraft `1.21.3` Fabric port are documented in this file.

## [1.0.0+1.21.3] - 2026-11-27

### Target Environment & Toolchain
- **Minecraft:** `1.21.3`
- **Fabric Loader:** `0.20.1`
- **Yarn Mappings:** `1.21.3+build.2`
- **Fabric API:** `0.114.1+1.21.3`
- **Java Toolchain:** JDK `21` (`D:\Java\Java-21`, `JavaVersion.VERSION_21`, `options.release = 21`)
- **Gradle / Loom:** Gradle `8.14.3` with Fabric Loom `1.10-SNAPSHOT` (`1.10.5`)
- **Output Artifact:** Standardized JAR naming (`archives_base_name=expandedstorage-rk`, `archivesName = "${project.archives_base_name}-fabric-${project.minecraft_version}"`), producing `build/libs/expandedstorage-rk-fabric-1.21.3-1.0.0.jar`.

### Code, BlockEntity, Registry, GUI, DataGen & Resource Alignment
- **Block & Item Registration (`ModBlocks`):**
  - Verified `ModBlocks.registerChest` with explicit `RegistryKey<Block>` (`RegistryKeys.BLOCK`) and `RegistryKey<Item>` (`RegistryKeys.ITEM`) keys, `.registryKey(blockKey)` on `AbstractBlock.Settings`, and `.registryKey(itemKey).useBlockPrefixedTranslationKey()` on `Item.Settings` against Minecraft `1.21.3`.
- **Chest Block & BlockEntity (`ExpandedChestBlock`, `ExpandedChestBlockEntity`, `ModBlockEntities`):**
  - Confirmed `ExpandedChestBlock` (`super(entityTypeSupplier, settings)`), `ExpandedChestBlockEntity` (`readNbt` / `writeNbt` with `RegistryWrapper.WrapperLookup`, dormant-when-closed `ChestLidAnimator`), and `ModBlockEntities` (`FabricBlockEntityTypeBuilder.create(...).build()`) compile cleanly with zero deprecation warnings on `1.21.3`.
- **GUI Rendering & Networking (`RenderCompat`, `NetworkCompat`, `ExpandedChestScreen`):**
  - Verified `RenderCompat.drawRegion` (`DrawContext#drawTexture(RenderLayer::getGuiTextured, ...)`), `NetworkCompat` (`ExtendedScreenHandlerType<T, ChestOpenData>` + `PacketCodec<RegistryByteBuf, ChestOpenData>`), and `ExpandedChestScreen` operate cleanly against Minecraft `1.21.3` and Fabric API `0.114.1+1.21.3`.
- **Access Widener & Data Generation (`expandedstorage.accesswidener`, `ModRecipeProvider`, `src/main/generated/`):**
  - Validated `src/main/resources/expandedstorage.accesswidener` (`mutable field net/minecraft/screen/slot/Slot x I` and `y I`) via `:validateAccessWidener`.
  - Executed `./gradlew runDatagen` against Minecraft `1.21.3`, confirming all 25 generated data pack JSON files (`recipe/`, `loot_table/blocks/`, `advancement/recipes/decorations/`, `tags/block/`) match the `1.21.3` schema.
- **Documentation & Metadata:**
  - Updated `gradle.properties`, `fabric.mod.json` (expanded to `"minecraft": "~1.21.3"`, `"fabricloader": ">=0.20.1"`, `"java": ">=21"`, `"fabric-api": ">=0.114.1+1.21.3"`), `README.md`, `docs/DOCUMENTATION_EN.md`, and `docs/DOCUMENTATION_VN.md`.

### Build & Runtime Verification
- Executed `./gradlew runDatagen` and `./gradlew clean build --refresh-dependencies` with zero compiler errors, zero deprecation warnings, and zero severe warnings (`BUILD SUCCESSFUL`).
- Executed `./gradlew runClient` on Minecraft `1.21.3` with JDK `21`, Fabric Loader `0.20.1`, and Fabric API `0.114.1+1.21.3`:
  - Confirmed `expandedstorage 1.0.0` loaded and logged `Expanded Storage initialized (6 chest tiers registered).`.
  - Confirmed `minecraft:textures/atlas/chest.png-atlas` (`512x256x3`) and `minecraft:textures/atlas/gui.png-atlas` (`1024x512x0`) stitched cleanly, OpenAL audio initialized, and zero Mixin errors or mod-originated exceptions occurred.
