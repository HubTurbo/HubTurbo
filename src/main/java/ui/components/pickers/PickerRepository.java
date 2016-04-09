package ui.components.pickers;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;

/**
 * This class represents a repository in RepositoryPicker.
 *
 * It handles the appearance of repository by storing whether the repository is currently selected by the user and
 * providing a way to get the visual representation of the repo.
 */
public class PickerRepository implements Comparable<PickerRepository> {

    private final String repositoryId;
    private boolean isSelected = false;

    private static final int REPO_LABEL_PREFERRED_WIDTH = 340;
    private static final Insets DEFAULT_REPO_LABEL_PADDING = new Insets(1);

    public static final String COMMON_REPO_LABEL_STYLE = "-fx-background-radius: 3; -fx-border-radius: 3; ";
    public static final String SELECTED_REPO_LABEL_STYLE = "-fx-background-color: lightgreen; -fx-border-color:black;";
    public static final String DEFAULT_REPO_LABEL_STYLE = "-fx-background-color: lightblue;";

    public PickerRepository(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public Node getNode() {
        Label repoLabel = new Label();
        repoLabel.setPrefWidth(REPO_LABEL_PREFERRED_WIDTH);
        repoLabel.setPadding(DEFAULT_REPO_LABEL_PADDING);

        if (isSelected) {
            repoLabel.setText(repositoryId);
            repoLabel.setStyle(COMMON_REPO_LABEL_STYLE + SELECTED_REPO_LABEL_STYLE);
        } else {
            repoLabel.setText(repositoryId);
            repoLabel.setStyle(COMMON_REPO_LABEL_STYLE + DEFAULT_REPO_LABEL_STYLE);
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
