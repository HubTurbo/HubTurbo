# Development Process

Notes:
* The development process is a simplified version of the one used in [TEAMMATES](https://github.com/TEAMMATES/repo/blob/master/devdocs/process.md). 
* The `master` branch always contains the latest stable code (may contain unreleased features/fixes).
* The `release` branch always contains the latest released version.

## New Contributors

* Join the [contributor mailing list](https://groups.google.com/forum/#!forum/hubturbo-contributors).
* Create a fork of the repo.

Fixing issues:

1. Select an issue to handle. For your first issue, select an issue labelled `difficulty.beginner`. Optionally, you may discuss the issue using the issue tracker to see if your intended solution is suitable. 
2. Create a branch off the current `master` named "X-short-description", where `X` is the issue number. For example, `2342-remove-println` for an issue named `Remove all unnecessary println statements`.
3. Implement your changes in the created branch. Run tests locally and ensure that there are no failures or style violations (use the `test` and `check` tasks in Gradle).
4. Push your changes to your fork.
5. Create a pull request against the [`master`](https://github.com/HubTurbo/HubTurbo) branch of the main repo.
    - The name of the PR should be in the format "TITLE #X", where `TITLE` is the title of the issue you selected, and `X` is its number. e.g. `Sorting order is incorrect #123`
    - The description of the PR should include the text "Fixes #X" e.g. `Fixes #123` [or something similar](https://github.com/blog/1506-closing-issues-via-pull-requests) to auto-close the issue when the PR is merged.
    - You don't have to wait until your changes are ready to do this. Feel free to use the PR to discuss any difficulties you run into, or any clarification you need.
6. After creating the PR, you can check its status on the CI service [here](https://travis-ci.org/HubTurbo/HubTurbo/pull_requests). Please ensure that all tests pass without any style violations.
    - If your PR includes additional functional code and this causes coverage to drop, either update an existing test to cover the added/fixed functionality or add a new test.
7. When ready, get someone on the dev team to review it by commenting on the PR.
8. The reviewer will merge it when he/she is satisfied.

If you need any clarification on the development process, you may also post in the [mailing list](https://groups.google.com/forum/#!forum/hubturbo-contributors).

## Developers

Similar to above, except for following differences:

1. You'll be pushing to the main repo. 
2. You can also create new issues, label issues, and review code from others.
3. When creating a PR, label it as `ongoing` and choose another core developer as the reviewer. Try to pick someone who is likely to know the code touched by the PR well.
4. When your PR is marked as `toMerge` by the reviewer, you should merge the PR yourself.
   * Merging should be done locally, not using GitHub.
   * Format for the merge commit: `[issue number] issue title` e.g. `[324] Add keyboard shortcut for creating an issue`
   * Perform a non-fast-forward merge. `git merge --no-ff -m "merge commit message" your-branch-name` is one way to accomplish this.
   * Ensure all tests, including unstable ones, are passing before you push the merge commit to the repo.
