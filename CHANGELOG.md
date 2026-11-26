# Changelog

All notable changes for the **Expanded Storage** Minecraft `1.20.5` Fabric port are documented in this file.

## [1.0.0+1.20.5] - 2026-11-26

### Target Environment & Toolchain
- **Minecraft:** `1.20.5`
- **Fabric Loader:** `0.20.1`
- **Yarn Mappings:** `1.20.5+build.1`
- **Fabric API:** `0.97.8+1.20.5`
- **Java Toolchain:** Upgraded from JDK `17` to JDK `21` (`D:\Java\Java-21`, `JavaVersion.VERSION_21`, `options.release = 21`)
- **Gradle / Loom:** Gradle `8.14.3` with Fabric Loom `1.10-SNAPSHOT` (`1.10.5`)
- **Output Artifact:** Standardized JAR naming (`archives_base_name=expandedstorage-rk`, `archivesName = "${project.archives_base_name}-fabric-${project.minecraft_version}"`), producing `build/libs/expandedstorage-rk-fabric-1.20.5-1.0.0.jar`.

### Code, BlockEntity, Networking, Registry, DataGen & Resource Migrations
- **BlockEntity NBT Serialization (`ExpandedChestBlockEntity`, `StorageCompat`):**
  - Updated `StorageCompat#readInventory` and `StorageCompat#writeInventory` to pass `RegistryWrapper.WrapperLookup` to `Inventories.readNbt(nbt, stacks, registries)` and `Inventories.writeNbt(nbt, stacks, registries)`.
  - Updated `ExpandedChestBlockEntity#readNbt(NbtCompound, RegistryWrapper.WrapperLookup)` and `writeNbt(NbtCompound, RegistryWrapper.WrapperLookup)` for Minecraft `1.20.5`.
  - Migrated `ExpandedChestBlockEntity` inventory stack-list accessors from `setInvStackList(...)` / `method_11282()` to Yarn `1.20.5+build.1` `setHeldStacks(...)` and `getHeldStacks()`.
- **ScreenHandler Networking (`NetworkCompat`):**
  - Migrated `ExtendedScreenHandlerType` registration to `new ExtendedScreenHandlerType<>(factory::create, ChestOpenData.PACKET_CODEC)` backed by `PacketCodec<RegistryByteBuf, ChestOpenData>`.
  - Migrated `ExtendedScreenHandlerFactory` from `writeScreenOpeningData(ServerPlayerEntity, PacketByteBuf)` to `ExtendedScreenHandlerFactory<ChestOpenData>#getScreenOpeningData(ServerPlayerEntity)`.
- **Block & Item Registration (`ModBlocks`, `ModBlockEntities`):**
  - Replaced deprecated `FabricBlockSettings.create()` and `new FabricItemSettings()` in `ModBlocks` with vanilla `AbstractBlock.Settings.create()` and `new Item.Settings()`.
  - Retained vanilla `BlockEntityType.Builder.create(...).build()` in `ModBlockEntities`.
- **DataGen Providers & Generated Data Packs (`ModLootTableProvider`, `ModRecipeProvider`, `src/main/generated/`):**
  - Updated `ModLootTableProvider` and `ModRecipeProvider` constructors to accept `CompletableFuture<RegistryWrapper.WrapperLookup>`.
  - Executed `./gradlew runDatagen` against `1.20.5`, regenerating all 25 data pack JSON files under `src/main/generated/`:
    - Recipes (`src/main/generated/data/expandedstorage/recipes/`): Updated result object schema to `{"count": 1, "id": "..."}` while preserving the plural `recipes/` directory for `1.20.5`.
    - Block Loot Tables (`src/main/generated/data/expandedstorage/loot_tables/blocks/`): Updated container custom-name preservation from `minecraft:copy_name` to `minecraft:copy_components` (`include: ["minecraft:custom_name"]`).
    - Recipe Unlock Advancements and Block Mining Tags regenerated for `1.20.5`.
- **Client GUI, Rendering & Access Widener:**
  - Validated `src/main/resources/expandedstorage.accesswidener` (`mutable field net/minecraft/screen/slot/Slot x I` and `y I`) under Loom `1.10.5` (`:validateAccessWidener`).
  - Confirmed `ExpandedChestScreen`, `RenderCompat`, and `ExpandedChestBlockEntityRenderer` compile and render cleanly on `1.20.5`.
- **Documentation & Metadata:**
  - Updated `gradle.properties`, `build.gradle`, `fabric.mod.json` (expanded to `"minecraft": "~1.20.5"`, `"fabricloader": ">=0.20.1"`, `"java": ">=21"`, `"fabric-api": ">=0.97.8+1.20.5"`), `README.md`, `docs/DOCUMENTATION_EN.md`, and `docs/DOCUMENTATION_VN.md`.

### Build & Runtime Verification
- Executed `./gradlew runDatagen` and `./gradlew clean build` with zero compiler errors, zero deprecation warnings, and zero severe warnings (`BUILD SUCCESSFUL`).
- Executed `./gradlew runClient` on Minecraft `1.20.5` with JDK `21`, Fabric Loader `0.20.1`, and Fabric API `0.97.8+1.20.5`:
  - Confirmed `expandedstorage 1.0.0` loaded and logged `Expanded Storage initialized (6 chest tiers registered).`.
  - Confirmed `minecraft:textures/atlas/chest.png-atlas` and `minecraft:textures/atlas/gui.png-atlas` stitched cleanly, OpenAL audio initialized, and zero Mixin errors or mod-originated exceptions occurred.
