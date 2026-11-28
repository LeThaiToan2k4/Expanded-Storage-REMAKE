# Changelog

All notable changes for the **Expanded Storage** Minecraft `1.21.9` Fabric port are documented in this file.

## [1.0.0+1.21.9] - 2026-11-28

### Target Environment & Toolchain
- **Minecraft:** `1.21.9`
- **Fabric Loader:** `0.20.1`
- **Yarn Mappings:** `1.21.9+build.1`
- **Fabric API:** `0.134.1+1.21.9`
- **Java Toolchain:** JDK `21` (`JavaVersion.VERSION_21`, `options.release = 21`)
- **Gradle / Loom:** Gradle `8.14.3` with Fabric Loom `1.13-SNAPSHOT` (`1.13.6`)
- **Output Artifact:** Standardized JAR naming (`archives_base_name=expandedstorage-rk`, `archivesName = "${project.archives_base_name}-fabric-${project.minecraft_version}"`), producing `build/libs/expandedstorage-rk-fabric-1.21.9-1.0.0.jar`.

### Changed
- **Vanilla Copper Chest Conflict Fix (`1.21.9+`):**
  - Preserved internal registry ID `expandedstorage:copper_chest` across blocks, items, block entities, textures, and loot tables so worlds upgrading from `1.20.1`-`1.21.8` retain all chests and stored contents.
  - Updated display names in `en_us.json` (`Expanded Copper Chest` / `Large Expanded Copper Chest`) and `vi_vn.json` (`Rương Đồng Mở Rộng` / `Rương Đồng Mở Rộng Lớn`) to distinguish the 36-slot tier from Vanilla's 27-slot `minecraft:copper_chest`.
  - Updated the `expandedstorage:copper_chest` shaped recipe in `ModRecipeProvider` to `"III"`, `"ICI"`, `"IBI"` using `minecraft:chest` at center (`C`), 7 `minecraft:copper_ingot` (`I`), and 1 `minecraft:copper_block` at slot 8 (`B`, bottom-center), with unlock criteria for chest, copper ingot, and copper block.
  - Kept the `expandedstorage:iron_chest` shaped recipe (`"III"`, `"ICI"`, `"III"`) strictly bound to `expandedstorage:copper_chest` surrounded by 8 `minecraft:iron_ingot` (Vanilla Copper Chest variants are not accepted in any recipe).
  - Removed the hardcoded local `org.gradle.java.home` path from tracked `gradle.properties`.

### Code, BlockEntityRenderState, ContainerUser, GUI Click & Resource Migration
- **Block & BlockEntity ContainerUser Migration (`ExpandedChestBlock`, `ExpandedChestBlockEntity`):**
  - Updated `ExpandedChestBlock` constructor to pass `SoundEvents.BLOCK_CHEST_OPEN` and `SoundEvents.BLOCK_CHEST_CLOSE` to the 4-argument `ChestBlock(Supplier, SoundEvent, SoundEvent, Settings)` constructor introduced in `1.21.9`.
  - Updated `ExpandedChestBlock#getComparatorOutput(BlockState, World, BlockPos, Direction)` and `world.isClient()` to match `1.21.9` `AbstractBlock` and `World` method signatures.
  - Migrated `ExpandedChestBlockEntity#onOpen(ContainerUser)`, `onClose(ContainerUser)`, `getViewingUsers()`, and `public boolean isPlayerViewing(PlayerEntity)` on `ViewerCountManager` to the `1.21.9` `ContainerUser` interaction range API.
- **BlockEntityRenderState & OrderedRenderCommandQueue Migration (`ExpandedChestBlockEntityRenderer`, `RenderCompat`):**
  - Migrated `ExpandedChestBlockEntityRenderer` to `BlockEntityRenderer<ExpandedChestBlockEntity, ExpandedChestBlockEntityRenderer.RenderState>` extending `ChestBlockEntityRenderState`.
  - Implemented `createRenderState()`, `updateRenderState(...)`, and `render(RenderState, MatrixStack, OrderedRenderCommandQueue, CameraRenderState)` using `ChestBlockModel` (`EntityModelLayers.CHEST`, `DOUBLE_CHEST_LEFT`, `DOUBLE_CHEST_RIGHT`), `SpriteHolder` (`ctx.spriteHolder()`), and `RenderCompat.submitChestModel` (`queue.submitModel(...)`).
- **GUI Click Record Migration (`ExpandedChestScreen`):**
  - Updated `ExpandedChestScreen#isClickOutsideBounds(double, double, int, int)`, `mouseClicked(Click, boolean)`, and `mouseReleased(Click)` to use the `net.minecraft.client.gui.Click` record introduced in `1.21.9`.
- **DataGen & Documentation:**
  - Executed `./gradlew runDatagen` against Minecraft `1.21.9` and Fabric API `0.134.1+1.21.9`, confirming all 25 generated data pack JSON files (`recipe/`, `loot_table/blocks/`, `advancement/recipes/decorations/`, and `tags/block/`).
  - Updated `gradle.properties`, `fabric.mod.json` (expanded to `"minecraft": "~1.21.9"`, `"fabricloader": ">=0.20.1"`, `"java": ">=21"`, `"fabric-api": ">=0.134.1+1.21.9"`), `README.md`, `docs/DOCUMENTATION_EN.md`, and `docs/DOCUMENTATION_VN.md`.

### Build & Runtime Verification
- Executed `./gradlew runDatagen build` with zero compiler errors, zero deprecation warnings, and zero severe warnings (`BUILD SUCCESSFUL`).
- Executed `./gradlew runClient` on Minecraft `1.21.9` with JDK `21`, Fabric Loader `0.20.1`, and Fabric API `0.134.1+1.21.9`:
  - Confirmed `expandedstorage 1.0.0` loaded and logged `Expanded Storage initialized (6 chest tiers registered).`.
  - Confirmed all 6 `assets/expandedstorage/items/*_chest.json` special item models baked cleanly without warnings, `minecraft:textures/atlas/chest.png-atlas` and `minecraft:textures/atlas/gui.png-atlas` stitched cleanly, OpenAL audio initialized, and zero Mixin errors or mod-originated exceptions occurred.
