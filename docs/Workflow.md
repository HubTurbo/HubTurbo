# Workflow

Notes:
* The development workflow is a simplified version of the one used in [TEAMMATES](https://github.com/TEAMMATES/repo/blob/master/devdocs/process.md). 
* The `master` branch always contains the latest stable code.

## Workflow for new contributors

First, 
* Join the [contributor mailing list](https://groups.google.com/forum/#!forum/hubturbo-contributors).
* Create a fork of the repo. 

Fixing issues:

1. Select an issue to handle. For your first issue, select an issue labelled `difficulty.beginner`. Optionally, you may discuss the issue using the issue tracker to see if your intended solution is suitable. 
2. Create a branch off the [`master`](https://github.com/HubTurbo/HubTurbo) branch. The branch should be named "IssueX", where `X` is the number of the issue. e.g. `Issue123`
3. Implement your changes in the created branch. Run tests locally and ensure that there are no failures or style violations (use the `test` and `check` tasks in Gradle). 
4. Push your changes to your fork.
5. Create a pull request against the [`master`](https://github.com/HubTurbo/HubTurbo) branch of the main repo.
    - The name of the PR should be in the format "TITLE #X", where `TITLE` is the title of the issue you selected, and `X` is its number. e.g. `Sorting order is incorrect #123`
    - The description of the PR should include the text "Fixes #X" e.g. `Fixes #123` [or something similar](https://github.com/blog/1506-closing-issues-via-pull-requests) to auto-close the issue when the PR is merged.
    - You don't have to wait until your changes are ready to do this. Feel free to use the PR to discuss any difficulties you run into, or any clarification you need.
6. After creating the PR, you can check the status of the PR on the CI (Travis) [here](https://travis-ci.org/HubTurbo/HubTurbo/pull_requests), please ensure that all tests pass without any style violations. 
    - If your PR includes additional functional code and coverage drops, either update an existing test to test for the added/fixed functionality or add a new test. 
7. When ready, get someone on the dev team to review it. You can request a review by adding a comment to the PR.
8. The reviewer will merge it when he/she is satisfied.

If you need any clarification on the workflow, you may also post in the [mailing list](https://groups.google.com/forum/#!forum/hubturbo-contributors).

## Workflow for developers
Similar to above, but you'll be pushing to the main repo. You can also create new issues, label issues, and review code from others.