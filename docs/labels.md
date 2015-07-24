# Labels

## Creating Labels <a name="labels"></a>

Labels are created through GitHub's UI.

- Select `New > Label` (<kbd>Ctrl</kbd> + <kbd>L</kbd>) from the menu.

## Adding/editing labels of an issue/PR

Pressing <kbd>L</kbd> after selecting an issue card brings up the Label Picker. 

![](images/labels/1.png?raw=true)

The Label Picker is split into 2 parts, the part above the text field shows the labels that are currently applied to the particular item. The part below the text field shows all the labels that current exist in the repository. 

Clicking on labels in either part toggles their state. 

![](images/labels/2.gif?raw=true)

Typing in the text field highlights the labels that contain the query. 

![](images/labels/3.png?raw=true)

Press <kbd>Space</kbd> to toggle the highlighted label and then you can move on to add/remove other labels. 

![](images/labels/4.png?raw=true)

There is support for label groups, typing `p.l` highlights `priority.low` and so on. For exclusive groups marked with a `.`, adding a label from that group will remove all previous labels from that group. 

[[images/labels/5.png]]

Finally, press `Confirm` or <kbd>Enter</kbd> to accept the label selection or press `Cancel` or <kbd>Escape</kbd> to cancel and close the Label Picker. 