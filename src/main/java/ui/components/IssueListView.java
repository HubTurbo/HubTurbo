package ui.components;

import backend.resource.TurboIssue;
import ui.GUIElement;

public class IssueListView extends NavigableListView<GUIElement> {

    public boolean areItemsEqual(GUIElement guiElement1, GUIElement guiElement2) {
        TurboIssue item1 = guiElement1.getIssue();
        TurboIssue item2 = guiElement2.getIssue();
        return item1.getId() == item2.getId() &&
                item1.getRepoId().equals(item2.getRepoId());
    }

}
