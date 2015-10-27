package ui.listpanel;

import backend.interfaces.IModel;
import backend.resource.Model;
import backend.resource.TurboIssue;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class ListPanelCell extends ListCell<TurboIssue> {
    private static final Logger logger = HTLog.get(ListPanelCell.class);

    private final IModel model;
    private final int parentPanelIndex;
    private final ListPanel parent;
    private final HashSet<Integer> issuesWithNewComments;
    private TurboIssue issue;

    public ListPanelCell(IModel model, ListPanel parent,
                         int parentPanelIndex, HashSet<Integer> issuesWithNewComments) {
        super();
        this.model = model;
        this.parent = parent;
        this.parentPanelIndex = parentPanelIndex;
        this.issuesWithNewComments = issuesWithNewComments;
        setAlignment(Pos.CENTER);
        getStyleClass().add("bottom-borders");
    }

    @Override
    public void updateItem(TurboIssue issue, boolean empty) {
        super.updateItem(issue, empty);
        if (issue == null) {
            return;
        }
        updateStyleToMatchStatus(issue);
        this.issue = issue;
        Optional<Model> currentModel = model.getModelById(issue.getRepoId());
        if (!currentModel.isPresent()) {
            // TODO: see issue #1089
            logger.error("Model is not present for issue " + issue.getId() + " at " + issue.getRepoId());
            setGraphic(getGraphic());
            return;
        }

        setGraphic(new ListPanelCard(currentModel.get(), issue, parent, issuesWithNewComments));
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
        return issue.getLabels();
    }

    public TurboIssue getIssue() {
        return issue;
    }

}
