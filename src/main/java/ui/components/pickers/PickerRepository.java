package ui.components.pickers;

import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * This class is used to represent a repo in RepositoryPicker.
 *
 * It contains selected attribute to indicate whether the repo is selected.
 * These attributes are used in order to produce appropriate label through getNode()
 */
public class PickerRepository implements Comparable<PickerRepository> {

    private static final String BALLOT_BOX = "☐";
    private static final String BALLOT_BOX_WITH_CHECK = "☑";

    private final String repositoryId;
    private boolean isSelected = false;

    public PickerRepository(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public Node getNode() {
        Label repoLabel = new Label();

        if (isSelected) {
            repoLabel.setText(BALLOT_BOX_WITH_CHECK + " " + repositoryId);
        } else {
            repoLabel.setText(BALLOT_BOX + " " + repositoryId);
        }

        return repoLabel;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PickerRepository)) {
            return false;
        }
        PickerRepository other = (PickerRepository) o;
        return repositoryId.equalsIgnoreCase(other.getRepositoryId());
    }

    @Override
    public int hashCode() {
        return repositoryId.toLowerCase().hashCode();
    }

    @Override
    public int compareTo(PickerRepository o) {
        return repositoryId.toLowerCase().compareTo(o.getRepositoryId().toLowerCase());
    }
}
