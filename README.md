# A fork of [ASM library](https://gitlab.ow2.org/asm/asm) ![official JetBrains project](http://jb.gg/badges/official.svg)

The repository contains a fork of [ASM library](https://gitlab.ow2.org/asm/asm) with a couple of IntelliJ-specific patches on top (see commits).

### Updating the repository to a new ASM release

0. Prerequisites: a UNIX host with JDK 11 or newer.

1. First, visit the [Tags](https://gitlab.ow2.org/asm/asm/tags) page to find a tag
   corresponding to the desired release (for example, `ASM_9_1`).

2. Then make sure your local copy of the repository is up to date. If you don't have the local copy yet, just clone
   the repository, otherwise "cd" into that directory and execute `git fetch && git reset --hard origin/master`.

3. Fetch changes and tags from upstream: `git fetch --tags https://gitlab.ow2.org/asm/asm.git`.

4. Apply JB patches on top of the desired tag: `git rebase --onto ASM_9_1 UPSTREAM_HEAD master`.
   Since merge conflicts are virtually inevitable, it is recommended to perform the rebase in the IDE
   (_Git | Rebase_, _Modify options | --onto_, new base: `ASM_9_1`, old base: `UPSTREAM_HEAD`).

5. Make sure everything works as expected (see ["Testing the updated library"](#Testing-the-updated-library)).

6. Update the `UPSTREAM_HEAD` tag to make it point to the release tag: `git tag -f UPSTREAM_HEAD ASM_9_1`.

7. Finally, push the updates to GitHub: `git push --tags --force origin master`.

### Testing the updated library

1. Run unit tests: `./gradle/gradlew test`.

2. Assemble artifacts: `./gradle/gradlew clean jar -Prelease ; ./gradle/gradlew -b intellij-deps-asm.gradle fatJar fatSources`.

3. Use the artifacts to test IDEA's compatibility with the new release - e.g. add them to the project
   as "ASM" library roots and remote-run the "Aggregator" build configuration.
