# Creating a Release

## Wrap up the milestone

- Close completed issues and put them under the appropriate milestone
- Move issues which weren't finished to the next milestone

## Merge into `release`

- Check out `release`
- Merge `master` into release

## Document changes

- Update the [changelog](changelog.md)

## Bump version numbers

- Update version number in [build.gradle](../build.gradle)
- Update version number in [`ui.UI`](../src/main/java/ui/UI.java)

## Commit and tag `release`

- Commit, then tag in the following format: `VMAJOR.MINOR.PATCH`
    - No leading zeroes (i.e. `1` instead of `01`)
    - Example: `V0.12.1`
- `git push`
- `git push --tags`

Further reading: [semantic versioning](http://semver.org/)

## Back to `master`

- Check out `master`
- Cherry-pick the tagged commit onto master
- `git push`

## Create and upload JAR

- `./gradlew clean shadowJar`
- The JAR will be in `build/libs/HubTurbo-x.x.x-all`
- Name it in the format: `resource-vMAJOR.MINOR.PATCH.jar`
- Upload it to [Releases](https://github.com/HubTurbo/HubTurbo/releases/new) under the tag you just created

## Enable automatic updates

- Update [HubTurbo.xml](https://github.com/HubTurbo/AutoUpdater/blob/master/HubTurbo.xml) (both `serverURI` and `version`)
- Commit in the `VMAJOR.MINOR.PATCH` format

## Housekeeping

- Delete old [releases](https://github.com/HubTurbo/HubTurbo/releases) so only the last 5 minor versions remain <br>
**Warning**: do not delete the version that [HubTurbo.xml](https://github.com/HubTurbo/AutoUpdater/blob/master/HubTurbo.xml) relies on! The latest public release may have been some time back
- Close the previous milestone
- Create a new milestone for the next release
