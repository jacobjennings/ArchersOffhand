# MCS-7 merge report

```text
Reviewed 567cfa2 + target 94c05ac
              |
       merge 10372ee
              |
    build PASS + classes PASS
              |
origin/feat/mc-26.1.2-rewrite
```

Merged commit: `10372ee36faa1a2b62fa34054f517feaa347ec7c`.
Reviewed commit: `567cfa23ec8deb28b1aa79cae6ebe86e8f1913d6`.
Task: [MCS-7](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-7).
Review: [MCS-12](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-12).

## Merge evidence

Fetched origin before the trial merge and again before the target push.
The source tip matched the reviewed commit on both fetches.
The review tip stayed at `6cb5173ee9fc4a18436e2f022ef36d9ec7fcffe6`.
Its committed report starts with “Merge.”
The target stayed at `94c05ac1d860c637b8838101f80331ee31ac44c5` until the push.
The worktree started clean at that target commit.

The trial used `git merge --no-commit --no-ff`.
It completed without conflicts and was aborted before the real merge.
The real merge used `git merge --no-ff`.
There were no conflict resolutions.
The current target harness guides were retained.
No review findings were changed.

The fast-forward push went only to origin.
After another fetch, both ancestry checks returned exit status 0:

```text
git merge-base --is-ancestor 567cfa23ec8deb28b1aa79cae6ebe86e8f1913d6 origin/feat/mc-26.1.2-rewrite
git merge-base --is-ancestor 10372ee36faa1a2b62fa34054f517feaa347ec7c origin/feat/mc-26.1.2-rewrite
```

The fetched target tip was `10372ee36faa1a2b62fa34054f517feaa347ec7c`.
Every Git command used the worktree path through `git -C`.
No deployment, publication, or release was run.

## Gate

Both commands ran in the foreground without a pipe.
Java 25 and Gradle 9.7.0 were used.
`TMPDIR` was `/tmp/mc-server-spinner-upper-workers`.
The Node executable was `/usr/bin/node`.
`CUDA_VISIBLE_DEVICES` was empty.

| Command | Result | Exact task summary |
| --- | --- | --- |
| `./gradlew build` | `BUILD SUCCESSFUL in 6s` | `7 actionable tasks: 7 executed` |
| `./gradlew classes` | `BUILD SUCCESSFUL in 511ms` | `2 actionable tasks: 2 up-to-date` |

The build output included `compileTestJava` and `test`.
Gradle printed no test-count summary.
Counts below sum the attributes of the three `TEST-*.xml` files from this build.
Those XML files are committed in [test-results](test-results/).

| Test class | Tests | Passed | Failed | Errors | Skipped |
| --- | --- | --- | --- | --- | --- |
| ArchersOffhandConfigTest | 4 | 4 | 0 | 0 | 0 |
| ProjectilePreferenceControllerTest | 1 | 1 | 0 | 0 | 0 |
| ProjectilePreferenceTokensTest | 4 | 4 | 0 | 0 | 0 |
| Total across 3 XML files | 9 | 9 | 0 | 0 | 0 |

JUnit does not report a todo count.
All three test classes passed.
No test class failed or was skipped.
The `node_modules/typescript` check did not print `DEPS_READY`.
This repository uses Java and Gradle, with no Node typecheck gate.
The Java compiler tasks ran successfully.

Before the gate, `/proc/loadavg` read `10.45 12.76 12.78`.
The process-state count found 0 tasks in state D.
A three-second `/proc/stat` sample measured 18.1 percent busy and 81.9 percent idle.
No deferral was needed.
The build reported the existing config screen deprecation warning.
It also reported deprecated Gradle features for Gradle 10.

Built jar: `build/libs/archersoffhand-2.0.2+26.3.jar`.
Its size was 110,226 bytes.
The jar remains local and is not committed.
No browser, graphical application, Minecraft client, or server was started.
There was no browser cleanup to perform.

## Size and launch evidence

Comparing added files from target `94c05ac` to the merge found one new file.
`git cat-file -s` measured that report at 5,412 bytes.
No new file exceeded 10 MB.
The total was below 50 MB.
The merge report and its small evidence files also fit both limits.

The hand launch bypassed the reported verdict-reader bug.
The original refusal is preserved in [merge-verdict-refusal.json](merge-verdict-refusal.json).
It records the review report commit and the missing-verdict refusal.
The committed review itself provides the first-line approval.

## RECOMMENDATIONS

Before a public release, adopt the review's player checks on Minecraft 26.3.
Check controls, config persistence, title-screen access, and crossbow offhand restoration.
Track the verdict-reader fix separately from this merge.
