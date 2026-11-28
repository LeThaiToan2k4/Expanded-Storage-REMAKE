# Changelog

All notable changes for the **Expanded Storage** Minecraft `26.1` Fabric port are documented in this file.

## [1.0.0+26.1] - 2026-11-28

### Target Environment & Toolchain
- **Minecraft:** `26.1` (Unobfuscated Official Mojang Mappings)
- **Fabric Loader:** `0.20.1`
- **Fabric API:** `0.145.1+26.1`
- **Java Toolchain:** JDK `25` (`D:\Java\Java-25`, `JavaVersion.VERSION_25`, `options.release = 25`)
- **Gradle / Loom:** Gradle `9.9.2` with Fabric Loom `1.19.4` (`net.fabricmc.fabric-loom` non-remapping plugin)
- **Output Artifact:** Standardized JAR naming (`archives_base_name=expandedstorage-rk`, `archivesName = "${project.archives_base_name}-fabric-${project.minecraft_version}"`), producing `build/libs/expandedstorage-rk-fabric-26.1-1.0.0.jar`.

### Changed
- **Vanilla Copper Chest Conflict Fix (`1.21.9+` / `26.1+`):**
  - Preserved internal registry ID `expandedstorage:copper_chest` across blocks, items, block entities, textures, and loot tables so worlds upgrading from `1.20.1`-`1.21.11` retain all chests and stored contents.
  - Maintained display names in `en_us.json` (`Expanded Copper Chest` / `Large Expanded Copper Chest`) and `vi_vn.json` (`Rương Đồng Mở Rộng` / `Rương Đồng Mở Rộng Lớn`) to distinguish the 36-slot tier from Vanilla's 27-slot `minecraft:copper_chest`.
  - Maintained the `expandedstorage:copper_chest` shaped recipe in `ModRecipeProvider` (`"III"`, `"ICI"`, `"IBI"`) using `minecraft:chest` at center (`C`), 7 `minecraft:copper_ingot` (`I`), and 1 `minecraft:copper_block` at slot 8 (`B`, bottom-center), with unlock criteria for chest, copper ingot, and copper block.
  - Kept the `expandedstorage:iron_chest` shaped recipe (`"III"`, `"ICI"`, `"III"`) strictly bound to `expandedstorage:copper_chest` surrounded by 8 `minecraft:iron_ingot` (Vanilla Copper Chest variants are not accepted in any recipe).

### Code, Unobfuscated Mojmap, GuiGraphicsExtractor, SubmitNodeCollector & DataGen Migration
- **Toolchain & Unobfuscated Mojmap Migration (`build.gradle`, `expandedstorage.accesswidener`):**
  - Upgraded Gradle Wrapper to `9.9.2`, Java target to `Java 25`, and Fabric Loom to `1.19.4` (`id 'net.fabricmc.fabric-loom'`) using unobfuscated `implementation` dependencies for `fabric-loader` and `fabric-api`.
  - Updated `expandedstorage.accesswidener` to `accessWidener v2 official`, widening `net/minecraft/world/inventory/Slot` (`x`, `y`) and `net/minecraft/client/gui/screens/inventory/AbstractContainerScreen` (`imageWidth`, `imageHeight`).
- **Compat, Block, BlockEntity & Registry Migration (`IdCompat`, `StorageCompat`, `NetworkCompat`, `ExpandedChestBlock`, `ExpandedChestBlockEntity`, `ModBlocks`, `ModBlockEntities`, `ModItemGroups`, `ModScreenHandlers`):**
  - Migrated `IdCompat` to `net.minecraft.resources.Identifier` (`fromNamespaceAndPath`, `withDefaultNamespace`).
  - Migrated `StorageCompat` to `ContainerHelper.loadAllItems` / `saveAllItems` (`ValueInput` / `ValueOutput`, `NonNullList<ItemStack>`) and `AbstractContainerMenu.getRedstoneSignalFromContainer`.
  - Migrated `NetworkCompat` to `ExtendedMenuType`, `ExtendedMenuProvider`, `StreamCodec`, and `RegistryFriendlyByteBuf`.
  - Migrated `ExpandedChestBlock` and `ExpandedChestBlockEntity` to `ChestBlock`, `DoubleBlockCombiner`, `CompoundContainer`, `ContainerOpenersCounter`, `ChestLidController`, `ContainerUser#getLivingEntity`, and `loadAdditional` / `saveAdditional`.
  - Migrated registries to `BuiltInRegistries`, `BlockBehaviour.Properties`, `Item.Properties`, and `FabricCreativeModeTab`.
- **Client Rendering & GUI Extraction Migration (`RenderCompat`, `ExpandedChestScreen`, `ExpandedChestBlockEntityRenderer`, `ExpandedStorageClient`):**
  - Migrated `ExpandedChestScreen` (`AbstractContainerScreen`) and `RenderCompat` to `GuiGraphicsExtractor` (`extractRenderState`, `extractBackground`, `extractLabels`, `blit` with `RenderPipelines.GUI_TEXTURED`, `text`, `setComponentTooltipForNextFrame`, `setTooltipForNextFrame`), `MouseButtonEvent`, and `SimpleSoundInstance.forUI`.
  - Migrated `ExpandedChestBlockEntityRenderer` to `BlockEntityRenderer<ExpandedChestBlockEntity, RenderState>` (`ChestRenderState`), `ChestModel` (`ModelLayers.CHEST`, `DOUBLE_CHEST_LEFT`, `DOUBLE_CHEST_RIGHT`), `SpriteGetter`, `SpriteId` (`Sheets.CHEST_SHEET`), `ChestRenderer.modelTransformation`, and `SubmitNodeCollector#submitModel` (`RenderTypes::entityCutout`).
- **DataGen Providers (`ModRecipeProvider`, `ModLootTableProvider`, `ModBlockTagProvider`):**
  - Migrated DataGen providers to `FabricPackOutput`, `RecipeProvider` (`shaped`, `SmithingTransformRecipeBuilder.smithing`), `FabricBlockLootSubProvider` (`createNameableBlockEntityTable`), and `FabricTagsProvider.BlockTagsProvider` (`valueLookupBuilder` with `BlockTags.MINEABLE_WITH_PICKAXE`).

### Build & Runtime Verification
- Executed `./gradlew runDatagen` and `./gradlew clean build` on JDK `25` with zero compiler errors, zero deprecation warnings, and zero severe warnings (`BUILD SUCCESSFUL`).
- Executed `./gradlew runClient` on Minecraft `26.1` with JDK `25`, Fabric Loader `0.20.1`, and Fabric API `0.145.1+26.1`:
  - Confirmed `expandedstorage 1.0.0` loaded and logged `Expanded Storage initialized (6 chest tiers registered).`.
  - Confirmed all 6 `assets/expandedstorage/items/*_chest.json` special item models baked cleanly without warnings, `minecraft:textures/atlas/chest.png-atlas` and `minecraft:textures/atlas/gui.png-atlas` stitched cleanly, OpenAL audio initialized, and zero Mixin errors or mod-originated exceptions occurred.
