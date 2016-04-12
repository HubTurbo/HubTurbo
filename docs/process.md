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

- **Developers**: fix issues assigned to them; can be contributors, committers, or senior developers
- **Reviewers**: assigned to pull requests; usually senior developers
- **Team Lead**: subsumes above roles, releases new versions, ensures that milestone plan is ready at the beginning of each milestone
- **Project Manager**: project coordination, approves pull requests

## Branches

* `master` contains the latest stable code (including unreleased features/fixes)
* `rc` contains the release candidate: the latest version which has not been released to the public
* `release` contains the latest version released to the public

## Issue Lifecycle

- An issue is created. It is accepted by being given a priority.
- The issue is taken up by a developer.
    - Senior developers should take only `priority.high` issues as far as possible.
- A corresponding PR is created.
- The issue is closed when the PR is merged.

## PR Lifecycle

- A PR corresponding to an issue is created and labelled as `ongoing`.
- It is labelled as `toReview` when the developer finishes their work.
- The reviewer labels it as `toMerge` when satisfied, or `ongoing` if further changes have to be made.
- The PM labels it as `mergeApproved` when satisfied, or `ongoing` if further changes have to be made.
- The PR is merged.

A PR should be merged within one release cycle, two at the most. The onus is on the developer to remind the reviewer to keep things moving. PRs still open after two weeks may be closed without merging.

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
    - To restart the build, you can open and close the PR, or push new commits (squashing them later if they were only for the purpose of restarting)
1. When ready, get a core team member to review it by commenting on the PR. Try to do this at least a few days before the weekly milestone, as the process takes time.
1. Evaluate the reviewer's suggestions and make changes accordingly.
1. The reviewer and PM will approve the PR before merging it.

If you need any clarification on the development process, you may also post in the [mailing list](https://groups.google.com/forum/#!forum/hubturbo-contributors).

### Developers

Similar to the contributor process, except for following differences:

1. You'll push to the main repo.
1. You can also create new issues, label issues, and review code from others.
1. When creating a PR, label it as `ongoing` and choose another core developer as the reviewer. Try to pick someone who is likely to know the code touched by the PR well.
1. When your PR is labelled as `toMerge` by the reviewer, its state will be verified by the PM.
1. When your PR is labelled as `mergeApproved`, merge the PR yourself (see the section on merging below).

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
- Proper javadoc comments on public classes, public methods, and nontrivial private methods
- Method names and comments not outdated as a result of changes
- Static imports used effectively
- `Optional` values checked before use
- Thread-safe
- Meaningful test cases
- Proper synchronisation in GUI tests (no `Thread.sleep` or `PlatformEx.waitOnFxThread` without good reason)

When you are satisfied with the quality of the changes in the PR, change the label to `toMerge`. At this point it will await approval.

If the PR was from a contributor, merge it only after it is approved.

## Merging a Pull Request

- Wait for the green light (the `mergeApproved` label) before merging.
- Merging should be done locally, not using GitHub.
    - Format for the merge commit: `[issue number] issue title` e.g. `[324] Add keyboard shortcut for creating an issue`
- Perform a non-fast-forward merge.
    - `git merge --no-ff -m "merge commit message" your-branch-name` is one way to accomplish this.
- Ensure all tests, including unstable ones, pass before you push the merge commit to `master`.

## Releasing a New Version

### Wrap up the milestone

- Put completed issues under current milestone
- Remove milestones for issues which weren't finished
- Determine if open PRs can be merged by the next milestone
    - Comment on open PRs requesting to merge by the next milestone
    - Close PRs that have not been making progress despite reminders
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
- Name it in the format: `HubTurbo.jar`
- Upload it to [Releases](https://github.com/HubTurbo/HubTurbo/releases/new) under the tag you just created

### Prepare next release candidate

#### Document changes

- Check out `master`
- Update the [changelog](changelog.md)

#### Bump version numbers

- Update version number in [build.gradle](../build.gradle)
- Update version number in [`ui.UI`](../src/main/java/ui/UI.java)
- Commit in the `VMAJOR.MINOR.PATCH` format
- `git push`

#### Enable automatic updates

- Update [HubTurboUpdate.json](https://raw.githubusercontent.com/HubTurbo/HubTurbo/master/HubTurboUpdate.json)
  - If the release is a different major, create a new JSON object containing `version` and `applicationFileLocation`. This is to allow updating data store when there is an update in major version.
- Commit in the `VMAJOR.MINOR.PATCH` format

#### Create release candidate

- Check out `rc`
- Merge `master` into `rc`
    + Commit in the `VMAJOR.MINOR.PATCH` format
- `git push`

### Housekeeping

- Delete old [releases](https://github.com/HubTurbo/HubTurbo/releases) so only the last 5 minor versions remain.
- **Warning**: do not delete the version that [HubTurboUpdate.json](https://raw.githubusercontent.com/HubTurbo/HubTurbo/master/HubTurboUpdate.json) relies on! The latest public release may have been some time back
