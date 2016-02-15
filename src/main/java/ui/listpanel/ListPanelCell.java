package ui.listpanel;

import backend.resource.TurboIssue;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import org.apache.logging.log4j.Logger;
import ui.GuiElement;
import util.HTLog;

import java.util.HashSet;
import java.util.List;

public class ListPanelCell extends ListCell<GuiElement> {

    private final int parentPanelIndex;
    private final ListPanel parent;
    private final HashSet<Integer> issuesWithNewComments;
    private GuiElement guiElement;

    public ListPanelCell(ListPanel parent,
                         int parentPanelIndex, HashSet<Integer> issuesWithNewComments) {
        super();
        this.parent = parent;
        this.parentPanelIndex = parentPanelIndex;
        this.issuesWithNewComments = issuesWithNewComments;
        setAlignment(Pos.CENTER);
        getStyleClass().add("bottom-borders");
    }

    @Override
    public void updateItem(GuiElement guiElement, boolean empty) {
        super.updateItem(guiElement, empty);
        if (guiElement == null) {
            return;
        }
        this.guiElement = guiElement;
        TurboIssue issue = guiElement.getIssue();
        updateStyleToMatchStatus(issue);

        setGraphic(new ListPanelCard(guiElement, parent, issuesWithNewComments));
        this.setId(issue.getRepoId() + "_col" + parentPanelIndex + "_" + issue.getId());
    }

    private void updateStyleToMatchStatus(TurboIssue issue){
        final String closedStyle = "issue-cell-closed";
        boolean isCurrentStyleClosed = getStyleClass().contains(closedStyle);
        if (!issue.isOpen()){
            if (!isCurrentStyleClosed) {
                getStyleClass().add(closedStyle);
            }
        } else {
            if (isCurrentStyleClosed) {
                getStyleClass().remove(closedStyle);
            }
        }
    }

    public List<String> getIssueLabels() {
        return guiElement.getIssue().getLabels();
    }

    public TurboIssue getIssue() {
        return guiElement.getIssue();
    }

    public GuiElement getGuiElement() {
        return guiElement;
    }
}
