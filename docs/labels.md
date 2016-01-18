# Labels

## Creating Labels <a name="labels"></a>

Labels are created through GitHub's UI.

- Select `New > Label` (<kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>L</kbd>) from the menu.

## Label Groups

HubTurbo supports label groups using two different delimiters. 

- `.` indicates an exclusive group, multiple labels from this kind of group cannot be applied (e.g. `priority.low`)
- `-` indicates a non-exclusive group, multiple labels from this kind of group can be applied (e.g. `feature-labels`)

## Adding/editing labels of an issue/PR

Pressing <kbd>L</kbd> after selecting an issue card brings up the Label Picker. 

![](images/labels/main.png?raw=true)

The Label Picker is split into 2 parts, the part above the text field shows the labels that are currently applied to the particular item. The part below the text field shows all the labels that current exist in the repository. 

Clicking on labels in either part toggles their state. 

![](images/labels/demo.gif?raw=true)

Typing in the text field highlights the labels that contain the query. 

![](images/labels/highlight.png?raw=true)

Press <kbd>Space</kbd> to toggle the highlighted label and then you can move on to add/remove other labels. 

![](images/labels/toggle.png?raw=true)

There is support for label groups, typing `p.l` highlights `priority.low` and so on. For exclusive groups marked with a `.`, adding a label from that group will remove all previous labels from that group. 

![](images/labels/groups.png?raw=true)

Finally, press `Confirm` or <kbd>Enter</kbd> to accept the label selection or press `Cancel` or <kbd>Escape</kbd> to cancel and close the Label Picker. 

Label changes are not sent to GitHub immediately to allow the user a short window of time to undo the changes. The `Undo` button on the notification reverts the label changes and pressing <kbd>Ctrl</kbd>+<kbd>z</kbd> will do the same. 