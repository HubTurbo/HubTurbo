package ui.listpanel;

import java.util.HashSet;
import java.util.Optional;

import backend.interfaces.IModel;
import backend.resource.Model;
import backend.resource.TurboIssue;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;

public class ListPanelCell extends ListCell<TurboIssue> {

    private final IModel model;
    private final int parentPanelIndex;
    private final ListPanel parent;
    private final HashSet<Integer> issuesWithNewComments;

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

        Optional<Model> currentModel = model.getModelById(issue.getRepoId());
        assert currentModel.isPresent() : "Invalid repo id " + issue.getRepoId()
            + " for issue " + issue.getId();
        setGraphic(new ListPanelCard(currentModel.get(), issue, parent, issuesWithNewComments));
        this.setId(issue.getRepoId() + "_col" + parentPanelIndex + "_" + issue.getId());
    }
}
