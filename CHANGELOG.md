# Changelog

All notable changes for the **Expanded Storage** Minecraft `1.20.3` Fabric port are documented in this file.

## [1.0.0+1.20.3] - 2026-11-26

### Target Environment & Toolchain
- **Minecraft:** `1.20.3`
- **Fabric Loader:** `0.20.1`
- **Yarn Mappings:** `1.20.3+build.1`
- **Fabric API:** `0.91.1+1.20.3`
- **Java Toolchain:** JDK `17` (`D:\Java\Java-17`, `JavaVersion.VERSION_17`, `options.release = 17`)
- **Gradle / Loom:** Gradle `8.14.3` with Fabric Loom `1.10-SNAPSHOT` (`1.10.5`)
- **Output Artifact:** Standardized JAR naming (`archives_base_name=expandedstorage-rk`, `archivesName = "${project.archives_base_name}-fabric-${project.minecraft_version}"`), producing `build/libs/expandedstorage-rk-fabric-1.20.3-1.0.0.jar`.

### Code, BlockEntity, GUI, DataGen & Resource Migrations
- **Chest Block (`ExpandedChestBlock`):**
  - Migrated double and single chest menu creation inside `screenRetriever` from `checkLootInteraction(PlayerEntity)` to `LootableInventory#generateLoot(PlayerEntity)`.
  - Verified `ExpandedChestBlock#getTicker` continues using `validateTicker(...)` cleanly in `1.20.3`.
- **BlockEntity & LootableInventory (`ExpandedChestBlockEntity`):**
  - Migrated `readNbt(NbtCompound)` from `deserializeLootTable(nbt)` to `LootableInventory#readLootTable(nbt)`.
  - Migrated `writeNbt(NbtCompound)` from `serializeLootTable(nbt)` to `LootableInventory#writeLootTable(nbt)`.
  - Updated inventory stack-list retrieval in `writeNbt` to `method_11282()` (the Yarn `1.20.3+build.1` unmapped name for `ChestBlockEntity`'s `DefaultedList<ItemStack>` accessor).
- **Client GUI & Rendering (`ExpandedChestScreen`, `RenderCompat`, `ExpandedChestBlockEntityRenderer`):**
  - Confirmed `ExpandedChestScreen#render` (`super.render(...)` -> `HandledScreen#renderBackground(DrawContext, int, int, float)`), `RenderCompat`, and `ExpandedChestBlockEntityRenderer` compile and render cleanly against `1.20.3` with no signature changes required.
- **Access Widener & Compat Layer:**
  - Validated `src/main/resources/expandedstorage.accesswidener` (`mutable field net/minecraft/screen/slot/Slot x I` and `y I`) under Loom `1.10.5` (`:validateAccessWidener`).
  - Confirmed `IdCompat`, `StorageCompat`, and `NetworkCompat` compile and operate cleanly on `1.20.3`.
- **DataGen & Data Packs (`src/main/generated/`):**
  - Executed `./gradlew runDatagen` against `1.20.3`, regenerating 5 shaped recipe JSONs (omitting default `show_notification`) and 7 recipe unlock advancements under `src/main/generated/data/expandedstorage/`, while keeping plural `recipes/` paths and `"item"` result keys.
- **Documentation & Metadata:**
  - Updated `gradle.properties`, `build.gradle`, `fabric.mod.json` (expanded to `"minecraft": "~1.20.3"`, `"fabricloader": ">=0.20.1"`, `"java": ">=17"`, `"fabric-api": ">=0.91.1+1.20.3"`), `README.md`, `docs/DOCUMENTATION_EN.md`, and `docs/DOCUMENTATION_VN.md`.

### Build & Runtime Verification
- Executed `./gradlew runDatagen` and `./gradlew clean build` with zero compiler errors and zero severe warnings (`BUILD SUCCESSFUL`).
- Executed `./gradlew runClient` on Minecraft `1.20.3` with Fabric Loader `0.20.1` and Fabric API `0.91.1+1.20.3`:
  - Confirmed `expandedstorage 1.0.0` loaded and logged `Expanded Storage initialized (6 chest tiers registered).`.
  - Confirmed `minecraft:textures/atlas/chest.png-atlas` and `minecraft:textures/atlas/gui.png-atlas` stitched cleanly, OpenAL audio initialized, and zero Mixin errors or mod-originated exceptions occurred.
