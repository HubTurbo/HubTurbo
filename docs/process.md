# Development Process

## Background Knowledge

**Must have**:

- Basic Java programming skills
- Basic Git knowledge

**Good to have**:

- JavaFX
- Java concurrency
- Familiarity with Java 8 features: streams, lambda expressions

You may not need these at the beginning, but be prepared to learn them along the way!

## Roles

- **Devs**: fix issues assigned to them, can be core team members or contributors
- **Reviewers**: assigned to pull requests, usually core team members
- **Team Lead**: subsumes above roles, releases new versions
- **Project Manager**: general project coordination

## Branches

* `master` contains the latest stable code (including unreleased features/fixes)
* `rc` contains the release candidate: the latest version which has not been released to the public
* `release` contains the latest version released to the public

## Submitting a Pull Request

### New Contributors

#### Preliminaries

* Join the [contributor mailing list](https://groups.google.com/forum/#!forum/hubturbo-contributors)
* Fork the repository

#### Fixing issues

1. Select an issue to handle. For your first issue, select an issue labelled `forFirstTimers`. For subsequent issues, prefer those labelled `forContributors`.
1. Discuss the issue if the requirements are unclear, and check that your intended solution is suitable.
1. Branch off `master`, with branch name "X-short-description", where `X` is the issue number. For example, `2342-remove-println` for an issue named `Remove all unnecessary println statements`.
1. Implement your changes in the created branch. [Run tests locally](workflow.md) and ensure that there are no failures. Remember to also run unstable tests!
1. Push your changes to your fork.
1. Create a pull request against the [`master`](https://github.com/HubTurbo/HubTurbo/tree/master) branch of the main repo.
    - The name of the PR should be in the format "TITLE #X", where `TITLE` is the title of the issue you selected, and `X` is its number. e.g. `Sorting order is incorrect #123`
    - The description of the PR should include the text "Fixes #X" e.g. `Fixes #123` [or something similar](https://github.com/blog/1506-closing-issues-via-pull-requests) to auto-close the issue when the PR is merged.
    - You don't have to wait until your changes are ready to do this. Feel free to use the PR to discuss any difficulties you run into, or clarify requirements.
1. After creating the PR, you can check its status on the CI service [here](https://travis-ci.org/HubTurbo/HubTurbo/pull_requests). Ensure that all tests and checks pass.
    - If your PR includes additional functional code and this causes coverage to drop, either update an existing test to cover the added/fixed functionality or add a new test.
1. When ready, get someone on the dev team to review it by commenting on the PR.
1. The reviewer will merge it when he/she is satisfied.

If you need any clarification on the development process, you may also post in the [mailing list](https://groups.google.com/forum/#!forum/hubturbo-contributors).

### Developers

Similar to above, except for following differences:

1. You'll push to the main repo.
2. You can also create new issues, label issues, and review code from others.
3. When creating a PR, label it as `ongoing` and choose another core developer as the reviewer. Try to pick someone who is likely to know the code touched by the PR well.
4. When your PR is marked as `toMerge` by the reviewer, merge the PR yourself (see the section on merging below).

## Reviewing a Pull Request

### Workflow

The reviewer should ensure the following:

- Branch and PR names follow conventions
- "Fixes #x" in description
- Documentation is updated
- CI passes with no drop in coverage
- No violations from static analysis tools
- Unstable tests pass offline
- No additions to linter exceptions and unstable tests without discussion/consideration

### Code

In general, look out for style issues, potential problems, areas in which code could be cleaned up, improved, or written to leverage existing code.

An exhaustive list of things to look out for really cannot be given here, but these are a few common ones.

- Sane names
- Self-explanatory method signatures
- Appropriate abstractions used, intent expressed clearly
- Overall SLAP, with methods in the right places
- Method names and comments not outdated as a result of changes
- `Optional` values checked before use
- Static imports used effectively
- Thread-safe
- Meaningful test cases
- Proper synchronisation in GUI tests (no `Thread.sleep` or `PlatformEx.waitOnFxThread` without good reason)

When you are satisfied with the quality of the changes in the PR, change the label to `toMerge`.

If the PR was from a contributor, you can merge it at this point.

## Merging a Pull Request

- Wait for the green light (the `toMerge` label) before merging.
- Merging should be done locally, not using GitHub.
- Format for the merge commit: `[issue number] issue title` e.g. `[324] Add keyboard shortcut for creating an issue`
- Perform a non-fast-forward merge. `git merge --no-ff -m "merge commit message" your-branch-name` is one way to accomplish this.
- Ensure all tests, including unstable ones, pass before you push the merge commit to `master`.

## Releasing a New Version

### Wrap up the milestone

- Put completed issues under current milestone
- Remove milestones for issues which weren't finished
- Close current milestone
- Open next milestone

### Release previous release candidate

#### Tag

- Check out `release`
- Merge `rc` into `release`
    + Commit message: `VMAJOR.MINOR.PATCH`
        * No leading zeroes (i.e. `1` instead of `01`)
        * Example: `V0.12.1`
        * Further reading: [semantic versioning](http://semver.org/)
- Tag in the same format
- `git push`
- `git push --tags`

#### Upload JAR

- `./gradlew clean shadowJar`
- The JAR will be in `build/libs`
- Name it in the format: `resource-vMAJOR.MINOR.PATCH.jar`
- Upload it to [Releases](https://github.com/HubTurbo/HubTurbo/releases/new) under the tag you just created

#### Enable automatic updates

- Update [HubTurbo.xml](https://github.com/HubTurbo/AutoUpdater/blob/master/HubTurbo.xml)
- Commit in the `VMAJOR.MINOR.PATCH` format

### Prepare next release candidate

#### Document changes

- Check out `master`
- Update the [changelog](changelog.md)

#### Bump version numbers

- Update version number in [build.gradle](../build.gradle)
- Update version number in [`ui.UI`](../src/main/java/ui/UI.java)
- Commit in the `VMAJOR.MINOR.PATCH` format
- `git push`

#### Create release candidate

- Check out `rc`
- Merge `master` into `rc`
    + Commit in the `VMAJOR.MINOR.PATCH` format
- `git push`

### Housekeeping

- Delete old [releases](https://github.com/HubTurbo/HubTurbo/releases) so only the last 5 minor versions remain.
- **Warning**: do not delete the version that [HubTurbo.xml](https://github.com/HubTurbo/AutoUpdater/blob/master/HubTurbo.xml) relies on! The latest public release may have been some time back
