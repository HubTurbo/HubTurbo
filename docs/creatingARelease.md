# Creating a Release

## Wrap up the milestone

- Put completed issues under current milestone
- Remove milestones for issues which weren't finished
- Close current milestone
- Open next milestone

## Release previous release candidate

### Tag

- Check out `release`
- Merge `rc` into `release`
    + Commit message: `VMAJOR.MINOR.PATCH`
        * No leading zeroes (i.e. `1` instead of `01`)
        * Example: `V0.12.1`
        * Further reading: [semantic versioning](http://semver.org/)
- Tag in the same format
- `git push`
- `git push --tags`

### Upload JAR

- `./gradlew clean shadowJar`
- The JAR will be in `build/libs`
- Name it in the format: `resource-vMAJOR.MINOR.PATCH.jar`
- Upload it to [Releases](https://github.com/HubTurbo/HubTurbo/releases/new) under the tag you just created

### Enable automatic updates

- Update [HubTurbo.xml](https://github.com/HubTurbo/AutoUpdater/blob/master/HubTurbo.xml)
- Commit in the `VMAJOR.MINOR.PATCH` format

## Prepare next release candidate

### Document changes

- Check out `master`
- Update the [changelog](changelog.md)

### Bump version numbers

- Update version number in [build.gradle](../build.gradle)
- Update version number in [`ui.UI`](../src/main/java/ui/UI.java)
- Commit in the `VMAJOR.MINOR.PATCH` format

### Create release candidate

- Check out `rc`
- Merge `master` into `rc`
    + Commit in the `VMAJOR.MINOR.PATCH` format
- `git push`

## Housekeeping

- Delete old [releases](https://github.com/HubTurbo/HubTurbo/releases) so only the last 5 minor versions remain.
- **Warning**: do not delete the version that [HubTurbo.xml](https://github.com/HubTurbo/AutoUpdater/blob/master/HubTurbo.xml) relies on! The latest public release may have been some time back
