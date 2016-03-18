package ui.components.pickers;

import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * This class is used to represent a repo in RepositoryPicker.
 *
 * It contains selected attribute to indicate whether the repo is selected.
 * These attributes are used in order to produce appropriate label through getNode()
 */
public class PickerRepository {

    private final String repositoryId;

    public PickerRepository(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public Node getNode() {
        Label repoLabel = new Label(repositoryId);
        return repoLabel;
    }
}
