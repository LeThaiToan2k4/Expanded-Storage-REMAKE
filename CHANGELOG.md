# Changelog

All notable changes for the **Expanded Storage** Minecraft `1.20.2` Fabric port are documented in this file.

## [1.0.0+1.20.2] - 2026-11-26

### Target Environment & Toolchain
- **Minecraft:** `1.20.2`
- **Fabric Loader:** `0.20.1`
- **Yarn Mappings:** `1.20.2+build.4`
- **Fabric API:** `0.91.6+1.20.2`
- **Java Toolchain:** JDK `17` (`D:\Java\Java-17`, `JavaVersion.VERSION_17`, `options.release = 17`)
- **Gradle / Loom:** Gradle `8.14.3` with Fabric Loom `1.10-SNAPSHOT` (`1.10.5`)
- **Output Artifact:** Standardized JAR naming via `archivesName = "${project.archives_base_name}-fabric-${project.minecraft_version}"` (`archives_base_name=expandedstorage-rk`), producing `build/libs/expandedstorage-rk-fabric-1.20.2-1.0.0.jar` and aligning Maven publication `artifactId` to `project.base.archivesName.get()`.

### Code, GUI, BlockEntity, DataGen & Resource Migrations
- **Block & BlockEntity (`ExpandedChestBlock`):**
  - Updated `ExpandedChestBlock#getTicker` to call `BlockWithEntity.validateTicker(type, this.getExpectedEntityType(), ExpandedChestBlockEntity::clientTick)` in place of the removed `checkType` helper from `1.20.1`.
  - Retained `ExpandedChestBlockEntity#readNbt(NbtCompound)` and `writeNbt(NbtCompound)` (without `WrapperLookup`, which is unchanged in `1.20.2`), zero server-tick overhead, and dormant client lid animation.
- **Client GUI (`ExpandedChestScreen`):**
  - Removed the obsolete 1-argument `this.renderBackground(context)` call in `ExpandedChestScreen#render`, delegating background dimming and `drawBackground(...)` to `super.render(context, mouseX, mouseY, delta)` (`Screen#render` -> `HandledScreen#renderBackground(DrawContext, int, int, float)`), verified against `1.20.2` class bytecode via `javap`.
  - Confirmed `RenderCompat` (`DrawContext#drawTexture`, `drawText`, `drawTooltip`, `chest.png` atlas sprites) and `ExpandedChestBlockEntityRenderer` (`ModelPart#render`) remain compatible with `1.20.2`.
- **Access Widener & Networking:**
  - Retained and validated `src/main/resources/expandedstorage.accesswidener` (`mutable field net/minecraft/screen/slot/Slot x I` and `y I`) during Loom `validateAccessWidener`.
  - Retained `NetworkCompat` (`ExtendedScreenHandlerType` / `ExtendedScreenHandlerFactory` over `PacketByteBuf`) and `StorageCompat` (`Inventories.readNbt` / `writeNbt`).
- **Build & DataGen (`build.gradle`, `ModRecipeProvider`, `src/main/generated/`):**
  - Migrated `ModRecipeProvider#generate` from `Consumer<RecipeJsonProvider>` to `net.minecraft.data.server.recipe.RecipeExporter`.
  - Removed `mustRunAfter("runDatagen")` from `processResources` in `build.gradle` to resolve a circular Gradle task dependency (`:classes -> :processResources -> :runDatagen -> :classes`) when executing `./gradlew runDatagen`.
  - Regenerated DataGen outputs via `./gradlew runDatagen` for `1.20.2` (updating the 7 recipe unlock advancements under `src/main/generated/data/expandedstorage/advancements/recipes/decorations/` while preserving plural `recipes/` paths and `"item"` recipe result keys).
- **Documentation & Metadata:**
  - Updated `gradle.properties`, `fabric.mod.json` (expanded to `"minecraft": "~1.20.2"`, `"fabricloader": ">=0.20.1"`, `"java": ">=17"`, `"fabric-api": ">=0.91.6+1.20.2"`), `README.md`, `docs/DOCUMENTATION_EN.md`, and `docs/DOCUMENTATION_VN.md`.

### Build & Runtime Verification
- Executed `./gradlew runDatagen` and `./gradlew clean build` with zero compile errors and zero severe warnings.
- Verified runtime startup via `./gradlew runClient` on Minecraft `1.20.2` with Fabric Loader `0.20.1` and Fabric API `0.91.6+1.20.2`:
  - Confirmed `expandedstorage 1.0.0` loaded cleanly (`Expanded Storage initialized (6 chest tiers registered).`).
  - Confirmed `minecraft:textures/atlas/chest.png-atlas` and `minecraft:textures/atlas/gui.png-atlas` stitched cleanly, OpenAL audio started, and zero Mixin errors or mod-originated exceptions occurred.
