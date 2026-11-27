# Changelog

All notable changes for the **Expanded Storage** Minecraft `1.21.5` Fabric port are documented in this file.

## [1.0.0+1.21.5] - 2026-11-27

### Target Environment & Toolchain
- **Minecraft:** `1.21.5`
- **Fabric Loader:** `0.20.1`
- **Yarn Mappings:** `1.21.5+build.1`
- **Fabric API:** `0.128.2+1.21.5`
- **Java Toolchain:** JDK `21` (`D:\Java\Java-21`, `JavaVersion.VERSION_21`, `options.release = 21`)
- **Gradle / Loom:** Gradle `8.14.3` with Fabric Loom `1.10-SNAPSHOT` (`1.10.5`)
- **Output Artifact:** Standardized JAR naming (`archives_base_name=expandedstorage-rk`, `archivesName = "${project.archives_base_name}-fabric-${project.minecraft_version}"`), producing `build/libs/expandedstorage-rk-fabric-1.21.5-1.0.0.jar`.

### Code, BlockEntity, Renderer, GUI, DataGen & Resource Migration
- **BlockEntity Renderer Camera Position Parameter (`ExpandedChestBlockEntityRenderer`):**
  - Updated `ExpandedChestBlockEntityRenderer.render` to override the `1.21.5` `BlockEntityRenderer#render(ExpandedChestBlockEntity, float, MatrixStack, VertexConsumerProvider, int, int, Vec3d)` method signature including the 7th `Vec3d cameraPos` parameter.
- **Data Generation & 1.21.5 Smithing Recipe Schema (`ModRecipeProvider`, `src/main/generated/`):**
  - Executed `./gradlew runDatagen` against Minecraft `1.21.5`, updating `netherite_chest_from_diamond_chest_smithing.json` and `netherite_chest_from_obsidian_chest_smithing.json` to the `1.21.5` `TransmuteRecipeResult` schema (`"result": { "id": "expandedstorage:netherite_chest" }`) and confirming all 25 generated data pack JSON files match `1.21.5`.
- **Registry, BlockEntity, Item Models & GUI Compatibility (`ModBlocks`, `ModBlockEntities`, `ExpandedChestBlock`, `ExpandedChestBlockEntity`, `RenderCompat`, `NetworkCompat`):**
  - Confirmed registry-keyed block and item settings (`ModBlocks`), `FabricBlockEntityTypeBuilder` (`ModBlockEntities`), `ExpandedChestBlockEntity` (`readNbt` / `writeNbt` with `RegistryWrapper.WrapperLookup`), `assets/expandedstorage/items/*_chest.json` (`minecraft:special` -> `minecraft:chest`), `DrawContext#drawTexture(RenderLayer::getGuiTextured, ...)` (`RenderCompat`), and `ExtendedScreenHandlerType` (`NetworkCompat`) compile cleanly with zero deprecation warnings on `1.21.5`.
- **Documentation & Metadata:**
  - Updated `gradle.properties`, `fabric.mod.json` (expanded to `"minecraft": "~1.21.5"`, `"fabricloader": ">=0.20.1"`, `"java": ">=21"`, `"fabric-api": ">=0.128.2+1.21.5"`), `README.md`, `docs/DOCUMENTATION_EN.md`, and `docs/DOCUMENTATION_VN.md`.

### Build & Runtime Verification
- Executed `./gradlew runDatagen` and `./gradlew clean build --refresh-dependencies` with zero compiler errors, zero deprecation warnings, and zero severe warnings (`BUILD SUCCESSFUL`).
- Executed `./gradlew runClient` on Minecraft `1.21.5` with JDK `21`, Fabric Loader `0.20.1`, and Fabric API `0.128.2+1.21.5`:
  - Confirmed `expandedstorage 1.0.0` loaded and logged `Expanded Storage initialized (6 chest tiers registered).`.
  - Confirmed all 6 `assets/expandedstorage/items/*_chest.json` special item models baked cleanly without warnings, `minecraft:textures/atlas/chest.png-atlas` (`512x256x3`) and `minecraft:textures/atlas/gui.png-atlas` (`1024x512x0`) stitched cleanly, OpenAL audio initialized, and zero Mixin errors or mod-originated exceptions occurred.
