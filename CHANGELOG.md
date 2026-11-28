# Changelog

All notable changes for the **Expanded Storage** Minecraft `26.1.1` Fabric port are documented in this file.

## [1.0.0+26.1.1] - 2026-11-28

### Target Environment & Toolchain
- **Minecraft:** `26.1.1` (Unobfuscated Official Mojang Mappings)
- **Fabric Loader:** `0.20.1`
- **Fabric API:** `0.145.4+26.1.1`
- **Java Toolchain:** JDK `25` (`D:\Java\Java-25`, `JavaVersion.VERSION_25`, `options.release = 25`)
- **Gradle / Loom:** Gradle `9.9.2` with Fabric Loom `1.19.4` (`net.fabricmc.fabric-loom` non-remapping plugin)
- **Output Artifact:** Standardized JAR naming (`archives_base_name=expandedstorage-rk`, `archivesName = "${project.archives_base_name}-fabric-${project.minecraft_version}"`), producing `build/libs/expandedstorage-rk-fabric-26.1.1-1.0.0.jar`.

### Changed
- **Vanilla Copper Chest Conflict Fix (`1.21.9+` / `26.1+`):**
  - Preserved internal registry ID `expandedstorage:copper_chest` across blocks, items, block entities, textures, and loot tables so worlds upgrading from `1.20.1`-`26.1` retain all chests and stored contents.
  - Maintained display names in `en_us.json` (`Expanded Copper Chest` / `Large Expanded Copper Chest`) and `vi_vn.json` (`Rương Đồng Mở Rộng` / `Rương Đồng Mở Rộng Lớn`) to distinguish the 36-slot tier from Vanilla's 27-slot `minecraft:copper_chest`.
  - Maintained the `expandedstorage:copper_chest` shaped recipe in `ModRecipeProvider` (`"III"`, `"ICI"`, `"IBI"`) using `minecraft:chest` at center (`C`), 7 `minecraft:copper_ingot` (`I`), and 1 `minecraft:copper_block` at slot 8 (`B`, bottom-center), with unlock criteria for chest, copper ingot, and copper block.
  - Kept the `expandedstorage:iron_chest` shaped recipe (`"III"`, `"ICI"`, `"III"`) strictly bound to `expandedstorage:copper_chest` surrounded by 8 `minecraft:iron_ingot` (Vanilla Copper Chest variants are not accepted in any recipe).

### Code, Unobfuscated Mojmap, GuiGraphicsExtractor, SubmitNodeCollector & DataGen Alignment
- **Toolchain & Unobfuscated Mojmap Alignment (`build.gradle`, `expandedstorage.accesswidener`):**
  - Verified Gradle `9.9.2`, Java target `Java 25`, and Fabric Loom `1.19.4` (`id 'net.fabricmc.fabric-loom'`) using unobfuscated `implementation` dependencies for `fabric-loader` (`0.20.1`) and `fabric-api` (`0.145.4+26.1.1`).
  - Verified `expandedstorage.accesswidener` (`accessWidener v2 official`), widening `net/minecraft/world/inventory/Slot` (`x`, `y`) and `net/minecraft/client/gui/screens/inventory/AbstractContainerScreen` (`imageWidth`, `imageHeight`).
- **Compat, Block, BlockEntity & Registry Alignment (`IdCompat`, `StorageCompat`, `NetworkCompat`, `ExpandedChestBlock`, `ExpandedChestBlockEntity`, `ModBlocks`, `ModBlockEntities`, `ModItemGroups`, `ModScreenHandlers`):**
  - Verified `IdCompat` (`net.minecraft.resources.Identifier`), `StorageCompat` (`ContainerHelper.loadAllItems` / `saveAllItems` with `ValueInput` / `ValueOutput`), and `NetworkCompat` (`ExtendedMenuType`, `ExtendedMenuProvider`, `StreamCodec`).
  - Verified `ExpandedChestBlock` and `ExpandedChestBlockEntity` (`ChestBlock`, `DoubleBlockCombiner`, `CompoundContainer`, `ContainerOpenersCounter`, `ChestLidController`, `ContainerUser#getLivingEntity`, `loadAdditional` / `saveAdditional`).
- **Client Rendering & GUI Extraction (`RenderCompat`, `ExpandedChestScreen`, `ExpandedChestBlockEntityRenderer`, `ExpandedStorageClient`):**
  - Verified `ExpandedChestScreen` (`AbstractContainerScreen`) and `RenderCompat` (`GuiGraphicsExtractor`, `RenderPipelines.GUI_TEXTURED`, `MouseButtonEvent`, `SimpleSoundInstance.forUI`) and `ExpandedChestBlockEntityRenderer` (`ChestRenderState`, `ChestModel`, `SpriteGetter`, `SpriteId`, `SubmitNodeCollector#submitModel` with `RenderTypes::entityCutout`) on Minecraft `26.1.1`.
- **DataGen & Documentation:**
  - Executed `./gradlew runDatagen` against Minecraft `26.1.1` and Fabric API `0.145.4+26.1.1`, confirming all 25 generated data pack JSON files (`recipe/`, `loot_table/blocks/`, `advancement/recipes/decorations/`, and `tags/block/`).
  - Updated `gradle.properties`, `fabric.mod.json` (expanded to `"minecraft": "~26.1.1"`, `"fabricloader": ">=0.20.1"`, `"java": ">=25"`, `"fabric-api": ">=0.145.4+26.1.1"`), `README.md`, `docs/DOCUMENTATION_EN.md`, and `docs/DOCUMENTATION_VN.md`.

### Build & Runtime Verification
- Executed `./gradlew runDatagen build` on JDK `25` with zero compiler errors, zero deprecation warnings, and zero severe warnings (`BUILD SUCCESSFUL`).
- Executed `./gradlew runClient` on Minecraft `26.1.1` with JDK `25`, Fabric Loader `0.20.1`, and Fabric API `0.145.4+26.1.1`:
  - Confirmed `expandedstorage 1.0.0` loaded and logged `Expanded Storage initialized (6 chest tiers registered).`.
  - Confirmed all 6 `assets/expandedstorage/items/*_chest.json` special item models baked cleanly without warnings, `minecraft:textures/atlas/chest.png-atlas` and `minecraft:textures/atlas/gui.png-atlas` stitched cleanly, OpenAL audio initialized, and zero Mixin errors or mod-originated exceptions occurred.
