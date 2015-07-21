# Creating a Release

## Wrap up the milestone

- Close completed issues and put them under the appropriate milestone
- Issues which weren't finished should be moved to the next milestone

## Document changes

- Update the [changelog](Changelog.md)

## Bump version numbers

- Update version number in [build.gradle](../build.gradle)
- Update version number in [`ui.UI`](../src/main/java/ui/UI.java)

## Tag

- Commit, then tag in the following format: `VMAJOR.MINOR.PATCH`
    - No leading zeroes (i.e. `1` instead of `01`)
    - Example: `V0.12.1`
- `git push`
- `git push --tags`

## Create and upload JAR

**Eclipse**

- `File` > `Export` > `Runnable JAR file`
- `Extract required libraries into generated JAR` **(important!)**
- Name the JAR in the format: `resource-vMAJOR.MINOR.PATCH.jar`
- Finish wizard

**Gradle**

- `./gradlew build`
- The JAR will be in `build/libs/HubTurbo-x.x.x-all`
- Name the JAR in the format: `resource-vMAJOR.MINOR.PATCH.jar`

**Afterwards**

- Upload JAR to [Releases](https://github.com/HubTurbo/HubTurbo/releases/new) under the tag you just created

## Enable automatic updates

- Update [HubTurbo.xml](https://github.com/HubTurbo/AutoUpdater/blob/master/HubTurbo.xml) (both `serverURI` and `version`)
- Commit in the same format as before

## Housekeeping

- Delete old [releases](https://github.com/HubTurbo/HubTurbo/releases) so only the last 5 minor versions remain
- **Warning**: do not delete the version that [HubTurbo.xml](https://github.com/HubTurbo/AutoUpdater/blob/master/HubTurbo.xml) relies on! The latest public release may have been some time back.