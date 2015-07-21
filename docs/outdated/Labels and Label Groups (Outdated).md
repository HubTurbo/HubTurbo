Labels provide a means of classifying issues. Effective use of labels will allow classes of issues to be easily identified and operated on.

Labels can themselves be grouped. This provides additional conveniences such as label exclusivity.

# Contents

- [Applying Labels to Issues](#applying)
- [Operations on Labels](#labels)
- [Operations on Label Groups](#groups)

# Applying Labels to Issues <a name="applying"></a>

There are a number of ways to associate labels with issues.

The easiest way is through the sidebar on the left: simply drag the label you want to apply onto the appropriate issue.

For a greater degree of control over which labels are applied (for example, if you want to apply a few at once), you could also click on the issue so it shows in the sidebar on the left. `Ctrl+L` or clicking on the Label field will bring up a dialog allowing you to select the right labels to apply.

The last way is to drag an issue across panels, utilising [filter expressions](Filtering-Issues/#application).

# Operations on Labels <a name="labels"></a>

Labels are visible in the appropriate tab on the side bar.

## Adding a Label
1. Right-click on a label group to bring up context menu.
2. Select _New Label_ in context menu.
3. Enter new label name and set label color in the Add Label Dialog.
4. Select _Done_.

## Editing a Label
1. Right-click on a label to bring up context menu.
2. Select _Edit Label_ in context menu.
3. Edit label name and set label color in the Edit Label Dialog.
4. Select _Done_.

## Deleting a Label
1. Right-click on a label to bring up context menu.
2. Select _Delete Label_ in context menu.

Note that there will be **no confirmation**, and **the label will be deleted immediately once you select _Delete Label_.** Deleting a label that is currently assigned to issues will cause it to be removed from those issues.

# Operations on Label Groups <a name="groups"></a>

There are two types of label groups in HubTurbo: **Exclusive** and **Non-exclusive**.

Exclusive label groups provide you with the option to have ‘one at a time’ label sets. An issue may only hold one label from the label group if the label group is exclusive. A label called _high_ in the exclusive group _priority_ will have its full name displayed as _priority.high_ in Github's web interface.

Non-exclusive label groups do not have this restriction, and it is possible to select multiple labels belonging to the same non-exclusive label group to apply to an issue. A label called _ui_ in the non-exclusive group _component_ will have its full name displayed as _component-ui_ in Github's web interface.

On HubTurbo, it is possible to see from an issue card the group a label belongs to by rolling your cursor over the label.

![](https://cloud.githubusercontent.com/assets/3119252/3947376/d8a791f2-268a-11e4-9ec0-3495e57bb3e6.png)

## Adding a Label Group
1. Select _New Label Group_ button.
2. Enter new label group name and set label group exclusivity. _Label group exclusivity can only be set at creation._
3. Select _Done_.

## Editing a Label Group
1. Right-click on a label group to bring up context menu.
2. Select _Edit Group_ in context menu.
3. Edit label group name in the Edit Label Group Dialog.
4. Select _Done_