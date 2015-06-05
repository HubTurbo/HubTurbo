package ui.issuepanel;

import java.util.HashSet;
import java.util.Optional;

import backend.interfaces.IModel;
import backend.resource.Model;
import backend.resource.TurboIssue;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;

public class IssuePanelCell extends ListCell<TurboIssue> {

    private final IModel model;
    private final int parentColumnIndex;
    private final IssuePanel parent;
    private final HashSet<Integer> issuesWithNewComments;

    public IssuePanelCell(IModel model, IssuePanel parent,
                          int parentColumnIndex, HashSet<Integer> issuesWithNewComments) {
        super();
        this.model = model;
        this.parent = parent;
        this.parentColumnIndex = parentColumnIndex;
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

        Optional<Model> currentModel = model.getModelById(issue.getRepoId());
        assert currentModel.isPresent() : "Invalid repo id " + issue.getRepoId()
            + " for issue " + issue.getId();
        setGraphic(new IssuePanelCard(currentModel.get(), issue, parent, issuesWithNewComments));
        this.setId(issue.getRepoId() + "_col" + parentColumnIndex + "_" + issue.getId());
    }
}
