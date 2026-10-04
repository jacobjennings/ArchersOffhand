# MCS-7: client build for Minecraft 26.3

Branch: `codex/mcs-7-archers-offhand-26-3`, head `07efa6b` at report time.

Card: http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-7

## Result

The mod compiles, passes the config unit tests, and builds a working jar for
Minecraft 26.3. No publish, no release, no tag, no in-game test.

Jar: `build/libs/archersoffhand-2.0.2+26.3.jar`. It weighs 110,226 bytes.
A sources jar sits beside it. Neither is committed.

## Versions set, and where each was verified

- Minecraft moved from 26.1.2 to **26.3**, confirmed as a release in the
  piston-meta version manifest.

- Fabric loader moved from 0.19.3 to **0.19.5**, from the loader list for
  26.3 on meta.fabricmc.net.

- Fabric API moved from 0.153.0+26.1.2 to **0.161.0+26.3**, from the
  fabric-api metadata on maven.fabricmc.net.

- `Mod Menu` moved from 18.0.0-beta.1 to **21.0.0**. The Modrinth api
  returned it for 26.3, and the jar fetched with HTTP 200 from
  maven.terraformersmc.com.

- YACL moved from 3.9.5+26.1-fabric to **3.9.7+26.3-fabric**. The Modrinth
  api returned it for 26.3, and the pom fetched with HTTP 200 from
  maven.isxander.dev.

- Loom moved from 1.16.3 to **1.18.2**, latest release in the fabric-loom
  metadata on maven.fabricmc.net, pom HTTP 200.

- Gradle wrapper moved from 9.4.0 to **9.7.0**, distribution HTTP 200 on
  services.gradle.org.

- The mod version string moved from 2.0.2+26.1.2 to 2.0.2+26.3. This is
  derived, not looked up.

Loom 1.16.3 ships no variant for the Gradle 9.4.0 plugin API. Loom 1.18.2
requires 9.7.0, so the wrapper had to move too. That is one file outside the
brief's list, `gradle/wrapper/gradle-wrapper.properties`. The build could
not resolve any Loom without it.

`fabric.mod.json` now depends on loader 0.19.5 or newer, on
`yet_another_config_lib_v3` 3.9.7+26.3-fabric or newer, and on
`modmenu` 21.0.0 or newer.

The minecraft predicate is now **~26.3**. That covers 26.3 and any later
26.3.x patch. It excludes the 26.4 snapshots.

## API changes made

1. The current screen moved off `Minecraft`, and `minecraft.screen` and
   `client.setScreen(...)` are gone in 26.3. The screen now lives on the
   `gui` object. Updated at `InventoryManager.java:16`,
   `OffhandHandler.java:62`, and `KeyBindingManager.java:81-86`. The check
   for null, then open, logic is unchanged, so behavior is identical.

2. `InputConstants.Type.KEYSYM` is gone. The type enum is now just
   `KEYBOARD` and `MOUSE`. `KeyMapping` gained a constructor that takes a
   raw key code and infers the keyboard type. The three registrations in
   `KeyBindingManager.java` use it. Unbound defaults still pass the value of
   `InputConstants.UNKNOWN`. That resolves to the same cached key the
   constant holds, so "unbound" display and detection keep working. The
   default O shortcut is unchanged.

3. No code changes were needed for `ContainerInput`, `Fireworks` or
   `PotionContents`. The SWAP click path, and the firework and potion
   component code in `ProjectileCatalog` and `ProjectileMatcher`, compiled
   as-is against 26.3.

4. The YACL screen code, including the dropdown subclass in
   `ProjectilePreferenceController`, compiles against YACL 3.9.7. Three
   calls to the deprecated `valueFormatter` produce warnings only. They
   still work, and I left them alone to keep behavior identical.

## Test results, quoted from the run

- `./gradlew classes`: BUILD SUCCESSFUL, zero errors.

- `./gradlew test --tests 'com.carloplayz.archersoffhand.config.*'`: BUILD
  SUCCESSFUL. Counts from the result XML, per class:
  `ArchersOffhandConfigTest` 4 tests. `ProjectilePreferenceControllerTest`
  1 test. `ProjectilePreferenceTokensTest` 4 tests. Nine in all, nothing
  skipped, nothing failed, no errors.

- `ArchersOffhandConfigTest` run alone, with the offline board variables
  set: 4 tests, 0 skipped, 0 failures, 0 errors.

- `./gradlew assemble`: BUILD SUCCESSFUL. See the jar name above.

## For a player to check in game

1. Pressing the unbound toggle and cycle keys does nothing. Both should
   show as unbound in the Controls screen. The key refactor should be
   confirmed against the real input layer.

2. The O shortcut opens the config screen from open air. It does nothing
   while any other screen is open.

3. In the config screen, the projectile dropdown lists names and filters
   as you type. A clicked row is committed. Save, reopen, and confirm the
   token persisted.

4. A crossbow shot in survival pulls the preferred projectile from
   inventory into the offhand with one swap click. It then restores the
   previous offhand item. The swap code is unchanged, but nothing here
   exercised the 26.3 server side.

5. Opening the config from the title screen, with no world loaded, still
   shows the catalog without a crash.

## Recommendations

- Have a player run the five checks above on a 26.3 client before this
  branch merges anywhere public.

- Move the three `valueFormatter` calls to the replacement YACL formatter
  in a later card. The warnings sit at lines 95, 146 and 253 of
  `ArchersOffhandConfigScreen.java`.

- The merge lane should watch the wrapper. A Gradle below 9.7.0 fails the
  build at plugin resolution time with a confusing variant error.

- If a 26.3.x patch ever changes item components, rerun the encoding and
  decoding paths first. That is the only part of this mod that reads item
  components deeply.
