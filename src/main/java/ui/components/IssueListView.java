package ui.components;

import backend.resource.TurboIssue;
import ui.GuiElement;

public class IssueListView extends NavigableListView<GuiElement> {

    public boolean areItemsEqual(GuiElement guiElement1, GuiElement guiElement2) {
        TurboIssue item1 = guiElement1.getIssue();
        TurboIssue item2 = guiElement2.getIssue();
        return item1.getId() == item2.getId() &&
                item1.getRepoId().equals(item2.getRepoId());
    }

}
