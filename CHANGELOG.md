# Changelog

All notable changes to **Expanded Storage** (`https://github.com/LeThaiToan2k4/Expanded-Storage-REMAKE`) are documented in this file.

## [1.0.0] - 2026-11-25

### Added
- Clean-room Fabric `1.20.1` implementation of **Expanded Storage** by `thaistoanf` (`com.thaistoanf.expandedstorage`), depending solely on Fabric Loader and Fabric API.
- Six progressive Vanilla-style chest tiers:
  - **Copper Chest** (`36` single / `72` double slots)
  - **Iron Chest** (`54` single / `108` double slots)
  - **Gold Chest** (`81` single / `162` double slots)
  - **Diamond Chest** (`108` single / `216` double slots, budget diamond + glass recipe)
  - **Obsidian Chest** (`108` single / `216` double slots, `1200.0` blast resistance)
  - **Netherite Chest** (`135` single / `270` double slots, `1200.0` blast resistance, fireproof item, dual Smithing Table paths from Diamond or Obsidian Chest)
- Full Vanilla chest mechanics: same-tier double chest merging, `Shift` isolated placement, waterlogging, lid open/close animations and sounds, cat and solid-block obstruction checks, Hopper `DoubleInventory` automation, and Redstone Comparator signal calculation.
- Dynamic 9-slice T-shape GUI (`gui_light.png` and `gui_dark.png`) with seamless chest-to-player-inventory border stitching and click-through transparent side flanks.
- Three instant in-game GUI layout modes: `ADAPTIVE`, `SPLIT_WING`, and `SMART_TABBED` with automatic small-screen fallback and cursor-stack preservation across tab clicks.
- Native 1-click Light / Dark GUI toggle persisted via Gson in `config/expandedstorage-client.json`.
- Dedicated `Expanded Storage` creative tab (`itemGroup.expandedstorage.main`), 3D item rendering (`BuiltinItemRendererRegistry`), Fabric DataGen providers (recipes, loot tables, mining tags), and complete English (`en_us`) and Vietnamese (`vi_vn`) localizations.

### Changed
- Replaced starter template build configuration, package structure, entrypoints, license, and metadata with `expandedstorage` (`expandedstorage-rk-fabric-1.20.1-1.0.0.jar`).

### Optimized
- **Zero Server Tick Overhead:** `ExpandedChestBlock.getTicker` returns `null` on the logical server; viewer counts update strictly on scheduled container interaction events.
- **Zero Idle Client Tick:** `ExpandedChestBlockEntity.clientTick` exits immediately whenever a chest is closed (`viewerCount == 0` and lid animation progress is `0.0f`).
- **Sprite Atlas Batching:** Stitched all 18 `64x64` chest textures into the Vanilla chest atlas to minimize draw calls in large storage rooms.
- **Dirty-Slot Sync Only:** Relies on Vanilla `ScreenHandler` slot delta packets rather than custom full-container payloads.

---

## Version Porting Reference (`1.20.1` -> `1.21.11+`)

When porting **Expanded Storage** to newer Minecraft / Fabric releases, `core/` (`ChestTier`, `GuiMode`, `LayoutCalculator`, `ClientConfig`) remains unchanged. Only the following isolated adapter and metadata files require updates:

1. **`gradle.properties` & `build.gradle`:**
   - Update `minecraft_version`, `yarn_mappings`, `loader_version`, `loom_version`, `fabric_version`, `java_version` (`21` for `1.20.5+`), and `archives_base_name`.
2. **`src/main/resources/expandedstorage.accesswidener`:**
   - Remove or keep `mutable field Slot x / y` depending on whether target mappings already expose `Slot.x` and `Slot.y` as mutable.
3. **`compat/IdCompat.java`:**
   - Swap `new Identifier(MOD_ID, path)` to `Identifier.of(MOD_ID, path)` in `1.21+`.
4. **`compat/StorageCompat.java`:**
   - Add `RegistryWrapper.WrapperLookup` parameter to `Inventories.readNbt` / `Inventories.writeNbt` in `1.20.5+` (and `ReadView` / `WriteView` in `1.21.6+`).
5. **`compat/NetworkCompat.java`:**
   - Migrate `ExtendedScreenHandlerType` and `ExtendedScreenHandlerFactory` from `PacketByteBuf` to custom `CustomPayload` + `PacketCodec` in `1.20.5+`.
6. **`compat/RenderCompat.java`:**
   - Update `DrawContext.drawTexture` to pass `RenderLayer::getGuiTextured` (or `RenderPipelines.GUI_TEXTURED`) in `1.21.2+`, and update `ModelPart.render` / `BlockEntityRenderer` state submission in `1.21.9+`.
7. **`datagen/` Providers:**
   - Update `FabricRecipeProvider`, `FabricBlockLootTableProvider`, and `FabricTagProvider.BlockTagProvider` constructors for `CompletableFuture<RegistryWrapper.WrapperLookup>` changes in `1.20.5+` / `1.21+`.
