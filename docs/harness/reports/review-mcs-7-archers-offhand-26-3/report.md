Merge.

Reviewed `codex/mcs-7-archers-offhand-26-3` at
`567cfa23ec8deb28b1aa79cae6ebe86e8f1913d6`.
Completed wave: `mcs-7-archers-offhand-26-3`.
Task: [MCS-7](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-7).
Review: [MCS-12](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-12).

## Acceptance

No blocking findings. The port meets the implementation brief.
I read the original brief and the worker report at
`docs/harness/reports/mcs-7-archers-offhand-26-3/report.md`.
The reported code head is an ancestor of the reviewed commit.

I compared the source with `origin/feat/mc-26.1.2-rewrite` at `94c05ac`.
The source branched from `a73b4c5` before two later target guidance commits.
The apparent guidance deletions in the two-tip diff are target-only changes.
The source commits did not change the harness guides.
The merge lane must retain the current target guidance.

The version, metadata, README, and client API changes stay within the brief.
The wrapper change is the documented exception allowed by the brief.
Loom 1.18.2 publishes a runtime variant requiring Gradle plugin API 9.7.0.
The new wrapper satisfies that requirement.
No license files or license headers changed.
No secrets, built artifacts, or caches were added to the source diff.
`git diff --check` passed.

## Findings

Suggestion: `docs/harness/reports/mcs-7-archers-offhand-26-3/report.md:9`.
Replace “working jar” with “built jar”.
The reported checks establish compilation and config behavior.
They do not establish crossbow behavior in a running client.
The report correctly lists the remaining player checks elsewhere.
This wording suggestion does not block acceptance.

## Independent checks

I built an isolated archive of the exact reviewed commit at
`/tmp/mc-server-spinner-upper-workers/archersoffhand-review-567cfa2-n4drwy7v`.
The source worktree was left untouched.
Java 25.0.4.1 and Gradle 9.7.0 were used with a writable Gradle home.
No graphical application, Minecraft client, or server was started.
No lab host was contacted.

`./gradlew classes build test --rerun-tasks` reported BUILD SUCCESSFUL.
I read the task output and the test result XML.
`ArchersOffhandConfigTest` passed 4 tests.
`ProjectilePreferenceControllerTest` passed 1 test.
`ProjectilePreferenceTokensTest` passed 4 tests.
All 9 passed with no failures, errors, or skips.

`./gradlew test --tests 'com.carloplayz.archersoffhand.config.*' --rerun-tasks`
reported BUILD SUCCESSFUL.
`./gradlew test --tests com.carloplayz.archersoffhand.config.ArchersOffhandConfigTest --rerun-tasks`
reported BUILD SUCCESSFUL with 4 tests and no failures, errors, or skips.
Both targeted runs used the brief's offline board environment settings.
The existing config screen deprecation warning remains.
Gradle also reports deprecated features for a future Gradle 10 upgrade.

Jar: `build/libs/archersoffhand-2.0.2+26.3.jar`.
Full path:
`/tmp/mc-server-spinner-upper-workers/archersoffhand-review-567cfa2-n4drwy7v/build/libs/archersoffhand-2.0.2+26.3.jar`.
Size: 110,226 bytes.
The packaged metadata declares version `2.0.2+26.3` and client environment.
The jar remains local and is not committed.

## Version verification

Every new dependency version was checked independently outside the cache.


- Minecraft 26.3 is a release in the [official manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json).

- Loader 0.19.5 appears in the [26.3 loader list](https://meta.fabricmc.net/v2/versions/loader/26.3).

- Fabric API 0.161.0+26.3 has a matching [published POM](https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.161.0+26.3/fabric-api-0.161.0+26.3.pom).

- Loom 1.18.2 has a matching [published POM](https://maven.fabricmc.net/net/fabricmc/fabric-loom/1.18.2/fabric-loom-1.18.2.pom).

- Loom's Gradle requirement is in its [published module metadata](https://maven.fabricmc.net/net/fabricmc/fabric-loom/1.18.2/fabric-loom-1.18.2.module).

- `Mod Menu` 21.0.0 appears for Fabric 26.3 in the [Modrinth version API](https://api.modrinth.com/v2/project/modmenu/version?game_versions=%5B%2226.3%22%5D&loaders=%5B%22fabric%22%5D).

- YACL 3.9.7+26.3-fabric appears for Fabric 26.3 in the [Modrinth version API](https://api.modrinth.com/v2/project/yacl/version?game_versions=%5B%2226.3%22%5D&loaders=%5B%22fabric%22%5D).

- Gradle 9.7.0 has a [published distribution checksum](https://services.gradle.org/distributions/gradle-9.7.0-bin.zip.sha256), and the wrapper ran that version.

Direct `Mod Menu` and YACL Maven requests returned HTTP 403 during this review.
The independent Modrinth checks confirmed both exact versions and target support.
The derived mod version is `2.0.2+26.3`.

## API and compatibility evidence

I inspected the changed source and Minecraft 26.3 bytecode with `javap`.
`Gui.screen()` returns the current screen field.
`Gui.setScreen(...)` performs the screen lifecycle transition.
The changed calls preserve the existing screen guards and opening path.
The raw-key `KeyMapping` constructor uses `InputConstants.Type.KEYBOARD`.
`InputConstants.UNKNOWN` uses the same type and cached key factory.
`KeyMapping.isUnbound()` compares against that UNKNOWN key.
The unbound defaults and the O constant therefore retain their intended meaning.

The selected Minecraft predicate is `~26.3`.
I exercised `Fabric Loader` 0.19.5's predicate parser without starting Minecraft.
It accepted 26.3 and 26.3.1 and rejected 26.2 and 26.4.
Packaged YACL and `Mod Menu` metadata also cover Minecraft 26.3.

The SWAP path and firework and potion component code were unchanged.
Their successful compilation does not prove runtime behavior.
The existing tests cover config behavior rather than input or inventory handling.

## RECOMMENDATIONS

Accept the source change into the native merge lane.
Retain the newer target branch harness guidance when combining branches.
Before a public release, have a player verify unbound controls, the O shortcut,
config selection and persistence, title-screen config access, and crossbow
projectile selection and offhand restoration on Minecraft 26.3.
No merge, deployment, release, or tag was performed by this review.
