## Issue Creator
A simple issue creator where you can create a new isue or edit an existing issue.

### Table of Contents
1. [`Getting Started`](#getting-started)
2. [`Architecture`](#architecture)
3. [`Dependencies`](#dependencies)
4. [`Improvements`](#improvements)

### Getting Started
1. To create a new issue, press `CTRL + N`. 
2. To edit an existing issue, press `I` on selected issue.
3. To start editing issue's description, simply double-click on the textarea
4. To toggle preview of issue's description, press `ALT + P`

> Info: Preview will be shown instead of raw content unless when the user is editing 

### Architecture
1. Adopts [Passive View](http://martinfowler.com/eaaDev/PassiveScreen.html) pattern where the [View](https://github.com/nus-fboa2016-ht/HubTurbo/blob/issue-creator/src/main/resources/ui/fxml/IssueCreatorView.fxml)
is updated by [Presenter](https://github.com/nus-fboa2016-ht/HubTurbo/blob/issue-creator/src/main/java/ui/components/issue_creators/IssueCreatorPresenter.java)
2. Utility classes:
 - [`IssueContentPane`](https://github.com/nus-fboa2016-ht/HubTurbo/blob/issue-creator/src/main/java/ui/components/issue_creators/IssueContentPane.java) models GitHub's content pane i.e comment and desription
 - [`SuggestionMenu`](https://github.com/nus-fboa2016-ht/HubTurbo/blob/issue-creator/src/main/java/ui/components/issue_creators/SuggestionMenu.java) is an extended `ContextMenu` for auto-completion of actions like `#reference` and `@mention`

### Dependencies
1. [Pegdown](https://github.com/sirthias/pegdown), markdown processor with support of GitHub flavored markdown syntax
2. [RichTextFX](https://github.com/TomasMikula/RichTextFX)(tentative), extended JavaFX `TextArea` with support for syntax highlighting and popup at caret position

### Improvements
1. Supports undo by extending Action
2. Link other components such as milestone picker, assignee picker and label picker to populate details of an Issue
3. Supports @mention and #reference in IssueContentPane 
4. Add GitHub markdown stylesheet to make Preview more similar to GitHub Issue Tracker 
5. Add button to close or reopen issue 
