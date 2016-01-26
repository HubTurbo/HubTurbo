
# Changelog

# V3.17.0

- Added tooltips to panel buttons and improved layout
- Changed icon used for updates to issue titles in feed
- Reworked undo for label changes
- Fixed race condition when performing actions on repositories
- Fixed bug involving missing resources in unstable tests
- Fixed bug where space key did not work in the filter field
- Fixed bugs with filter field autocompletion
- Refined developer documentation
- Fixed errors in user guide

# V3.16.0

- Added an application icon for Windows and OS X
- Added a few panel templates
- Fixed bug with navigating to the related PR of an issue
- Improved appearance of closed issue titles
- Improved testing infrastructure (integrated Mockito)
- Improvements to filter expression parser
- Integrated PMD

# V3.15.0

- Added buttons to confirm and revert panel renaming

# V3.14.0

- Long panel names now wrap
- Fixed regression in milestone aliases
- Tests which involve dragging panels are now more reliable

# V3.13.0

- Added keyboard shortcut for swapping panels
- Added keyboard shortcut for navigating to/from related issues/PRs
- Fixed bug where milestone aliases wouldn't work if there was no open milestone
- Restructuring of metadata update process

# V3.12.0

- Milestone and work allocation boards can be automatically generated
- Closed issues are now shown in a different colour
- Fixed bug where saved repositories were sometimes being deleted
- Fixed bug where panel UI would go blank when switching repo
- Fixed bugs with panel selection in tests
- Upgraded to ControlsFX 8.40.10

# V3.11.0

- Users can now refer to milestones with aliases
- Removable repos are now indicated as such
- The close button won't be shown when panels are being renamed
- Fixed paging bug which caused missing milestones
- Fixed bug where repos were sometimes not saved after updating
- Improved testing support for typing strings

# V3.10.0

- Issues can now be sorted by milestone
- Keyboard shortcuts are now consistent with OS X conventions (using <kbd>⌘</kbd> instead of <kbd>Ctrl</kbd>)
- Board names are now validated
- Added a means of navigating PR tabs without leaving the panel view
- Label positions in the label picker are now stable when typing
- Fixed bug with label picker undo window which could lead to inconsistent model state
- Fixed bug where repo list wasn't updated when a repo was added
- Fixed bug where API requests for pull requests were unauthenticated
- Unstable tests are no longer run on Travis
- Improved documentation for `sort`

# V3.9.0

- Review comments are now taken into account by the `updated` qualifier
- The browser view will scroll to the bottom on loading an issue
- Fixed bug with hotkey for switching repository

# V3.8.0

- Fixed unresponsive <kbd>Ctrl</kbd> keybindings
- Fixed a bug where I/O errors could go unreported

# V3.7.1

- Temporarily removed `updated-self` and `updated-others`
- Fixed bug where adding labels quickly caused undo not to work properly
- Improvements to testing infrastructure

# V3.7.0

- <kbd>Ctrl</kbd> + <kbd>1</kbd> .. <kbd>9</kbd> will jump to the nth issue
- <kbd>Ctrl</kbd> + <kbd>Enter</kbd> and <kbd>Ctrl</kbd> + <kbd>F</kbd> will move focus to the panel and filter field respectively; <kbd>Space</kbd> <kbd>Space</kbd> is no longer the default hotkey for this
- Fixed bugs with panel focus
- Infrastructure for supporting updates to review comments

# V3.6.1

- Fixed bug where issue event metadata was not being downloaded
- Fixed bug with locale-specific dates

# V3.6.0

- Label picker changes are now delayed and can be undone
- Issues can be sorted by status
- Issues can be filtered more precisely by self/non-self updates
- Open repositories can be closed
- The title bar now shows the name of the currently-open board
- Issue metadata ETags are now saved, optimising quota use
- Fixed bug where issues would become unread after a self update
- Fixed bug where unread issues would appear as read
- Fixed bug where the first panel did not always have focus on startup
- Additional testing infrastructure

# V3.5.1

- Reverted addition of Chrome custom profile
- Repositories can now be removed

# V3.5.0

- A Chrome custom profile now persists users' sessions in the browser view
- Added hotkeys to jump directly to issues
- Added keyboard shortcut for changing board
- Updated chromedriver to 2-18
- Fixed globalConfigTest failing randomly on CI
- Fixed bug with certain resources being detected as unchanged due to not considering paged ETags

# V3.4.0

- Changes to labels via the label picker can now be undone
- Panels can now be sorted by assignee
- Boards can now be saved without having to specify a name if there already is one
- <kbd>Esc</kbd> removes focus from the repo selection dropdown
- Fixed hotkey conflict between <kbd>R</kbd> and <kbd>Ctrl</kbd> + <kbd>R</kbd>
- Fixed bug where label ordering caused issues not to matched

# V3.3.0

- Panels can now be given names
- Label updates are now aggregated
- Label picker now distinguishes existing labels and new ones
- Fixed bug where metadata updates were not shown
- Fixed bug where closing a panel wouldn't put focus on another panel
- Default comment hotkey changed to <kbd>R</kbd> (was formerly <kbd>C</kbd>)
- Adjusted coding conventions on braces

# V3.2.0

- Fixed bug with label picker going off-screen
- Fixed bug where an abbreviated string wouldn't match a label
- Fixed documentation of keyboard shortcut for Mark As Read
- Findbugs integrated into build process

# V3.1.0

- Polished and standardized user and developer documentation
- Added visual support for groups in label picker
- Improved support for older GitHub repositories
- Added support for 64-bit Linux chromedriver
- bView no longer takes away focus from pView on Windows
- Fixed bug where the login dialog redownloads the primary repository if wrong casing is specified
- Additional detection and recovery mechanisms for local cache corruption

# V3.0.0

- Added the label picker
- Documentation is now part of the main repository
- Removed all `.jar` dependencies from the repository
- Streamlined the user login process
- Allowed user customisation of keyboard shortcuts
- Fixed issues with the repository selector
- Rewrite of GUI logic
- More robust testing
- Added detection for `json` corruption
- UI now shows remaining GitHub API calls and time till next refresh

# V2.9.0

- `updated` filter now differentiates between issue event actors by ignoring own events
- Attached implicit sorting by last updated time to `updated` filter
- Remapped keyboard shortcut for mark as read
- Corrected keyboard shortcut for creating left panel
- More robust switching between bView and pView
- Test cases are now compatible with Windows
- Eclipse project files fixed

# V2.8.0

- Re-enabled feed
- Read and unread status for issues
- `id` qualifier now supports number ranges
- New `sort` qualifier, which supports sorting by many different keys
- Status bars for repository update operations
- Repositories now refresh periodically
- Fixed repository updates not being propagated
- Fixed bugs with labels and other resources not appearing
- UI optimisations
- Fixed quirky browser behaviour and made it open on logic instead of on first click
- User config directory is now called `settings`
- CI and extensive test coverage

# V2.7.0

- Rewrite of back end
- Multiple repositories can be loaded concurrently via the `repo` qualifier
- A few missing features: feed, visual feedback, drag-and-drop

# V2.6.0

- Cleaned up login process; browser is only shown on clicking an issue
- Fixed progress bar updates being out of sync
- Fixed issues with focus and hotkeys

# V2.5.0

- Error handling for exceeding the API rate limit
- Fixed assertion failures on startup
- Fixed bugs where browser view would restart when clicking on an issue
- Fixed focus issues with double space and the filter box
- Fixed navigation keys sometimes not working

# V2.4.2

- Fixed bug where closing a panel wouldn't transfer focus to another panel
- Fixed bug where double space wouldn't transfer focus to the issue list

# V2.4.1

- Fixed bug where more than 5 log files were produced
- Fixed bug with space not applying filters

# V2.4.0

- Fixed a deadlock caused by switching repo right as the sync timer triggers
- All progress dialogs now show the progress of downloads
- Fixed F5 not working in filter box
- Refined Esc behaviour in filter box

# V2.3.0

- Fixed long-standing issues with syncs taking much longer than required
- HubTurbo will automatically log in after the first time
- Cache will temporarily be deleted on update
- Implemented navigation shortcuts involving the filter box
- Panels will now be scrolled to when navigated to with hotkeys
- Fixed bug with `is:issue`
- Fixed bugs with some hotkeys not working when focus is on issue list
- Fixed bugs with double space hotkey
- Fixed bug where no panel would have focus on startup/switching project

# V2.2.0

- Implemented more navigation shortcuts
- f/b now cycle back
- Fixed bug where changes to issues weren't always being received
- Fixed bug where clicking on panels themselves wouldn't give focus

# V2.1.2

- Upgraded to JavaFX 8u40
- Implemented navigation shortcuts
- Fixed bug where project-switcher wouldn't show selected project

# V2.1.1

- `updated` filter no longer causes issue events to be downloaded on the UI thread
- Ensured that chromedriver is updated when HubTurbo is installed via updater

# V2.1.0

- Updated chromedriver to 2.14
- Fixed bug where browser view stopped responding after showing alerts
- Fixed bug where the main HubTurbo application window wouldn't receive focus
- Number of log files is now limited to 5
- HubTurbo will only sync if the browser view has changed
- Added additional logging and safety checks in case browser view stops responding

# V2.0.1

- Fixed bug where sync timer was at 10 seconds instead of 60
- Fixed issue with clicking project-switching dropdown still causing a sync on Windows sometimes

# V2.0.0

- Fixes for concurrency issues, such as when syncs happen repeatedly despite only being triggered once
- Syncs won't be triggered if the project-switching box is clicked to bring HT into focus
- Added hotkey for jumping to the comment box in the browser
- When the browser view is reopened, the issue that caused the event will be shown

# V1.9.4

- Fixed bug with wrong window getting focus
- Fixed bug where the window wouldn't get focus if minimized

# V1.9.3

- Fixed wrong Chrome window getting focus

# V1.9.2

- Fixed crashes on OS X due to Windows-specific code
- Fixed bug where updates happened more often than intended

# V1.9.1

- Hotfix for index out of bounds exception

# V1.9.0

- Feed now contains comment updates
- Dragging issues into filter box now shows similar issues
- Issues with new comments will be highlighted until clicked
- Browser window gains focus when appropriate
- Added keyboard shortcut for jumping between filter box and issue cards
- More details in logs

# V1.8.0

- Update timer is reset on every focus-triggered sync, and paused during syncing
- More visual feedback when syncing, including new status bar
- F5 will now trigger refresh even if focus is not on the issue list
- Added failsafes for browser disconnection

# V1.7.1

- Automatic updates now pause while an update is in progress
- The repository combobox now shows when an update is in progress

# V1.7.0

- Fixed bug where panels did not show new issues
- Log file size limited
- Fixed bug with attempting to close the application before logging in
- Upgraded ControlsFX and fixed visual bugs with dialogs
- Renamed "Panel Sets" to "Boards"

# V1.6.1

- Fixed bug where corruption of the cache could happen upon switching project

# V1.6.0

- Fixed bug where chromedriver wasn't extracted on startup
- Fixed bugs with modifier keys in the filter box on OS X
- Browser view now only initialises after login and no longer resizes sometimes afterwards
- Added unit tests for login process

# V1.5.0

- Sets of panels can now be saved
- Issue cards now show comment count
- Issue status changes are now shown in feed
- Fixed bug where `Ctrl+W` wouldn't close newly-created columns

# V1.4.0

- Refactoring
- Labels are no longer created automatically
- Project configuration has been removed
- Fixed bug where application wouldn't close when login button was clicked
- Periodic updates now start from when previous ones end rather than occur at a fixed rate

# V1.3.0

- `updated` qualifier shows issue changes in issue panel
- Improved appearance of login dialog
- Login dialog now remembers basic details
- Autocompletion includes collaborator names
- Fixes for high memory usage over time due to concurrency issues
- Columns are now known as panels
- `Ctrl+W` now deletes the right panels

# V1.2.0

- Side panel is hidden
- `updated` can now take a number and will default to `<` in that case

# V1.1.0

- `updated` filter qualifier
- Force Refresh now shows a progress dialog
- Fixed bug with issues being wrongly identified as pull requests
- More logging

# V1.0.0

- Documentation page is now shown in the browser instead of a webview
- Pull requests are differentiated from issues by an icon in the card

# V0.9.0

- Updater now integrates with HubTurbo and will show this changelog on startup if an update occurred
- Fixed bugs with issue status not updating, and with high memory usage
- Fixed bugs with selection changing wrongly on refresh
- Logs now contain more relevant information

# V0.8.1

- Fixed bugs with selection being reset on refresh, jumping to the wrong items, and not being registered on click when window is not in focus
- Made parser stricter when dealing with empty qualifiers
- Improved appearance of login dialog

# V0.8.0

- Basic feed

# V0.7.21

- Bug fixes
- Focus now remains on selected issues when switching window and refreshing

# V0.7.20

- Bug fixes

# V0.7.19

- Bug fixes for filters
- Added more filter qualifiers

# V0.7.18

- Overhauled filters
- Users can now access public repositories which they might not have push access to
