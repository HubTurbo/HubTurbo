package ui.components.pickers;

import backend.resource.TurboUser;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PickerAssignee extends TurboUser implements Comparable<PickerAssignee> {

    private static final int AVATAR_SIZE = 30;
    private static final int LABEL_HEIGHT = 40;

    boolean isExisting = false;
    boolean isFaded = false;
    boolean isHighlighted = false;
    boolean isSelected = false;

    public PickerAssignee(TurboUser user) {
        super(user);
    }

    public Node getNode() {
        Label assignee = new Label(getLoginName());
        assignee.setGraphic(getAvatarImageView());
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = fontLoader.computeStringWidth(assignee.getText(), assignee.getFont());
        assignee.setPrefWidth(width + 50 + AVATAR_SIZE);
        assignee.setPrefHeight(LABEL_HEIGHT);
        assignee.getStyleClass().add("labels");
        assignee.setStyle("-fx-background-color: yellow;");

        if (isSelected) {
            assignee.setText(assignee.getText() + " ✓");
        }

        if (isHighlighted) {
            assignee.setStyle(assignee.getStyle() + "-fx-border-color: black;");
        }

        if (isFaded) {
            assignee.setStyle(assignee.getStyle() + "-fx-opacity: 40%;");
        }

        return assignee;
    }

    public Node getNewlyAssignedAssigneeNode(boolean hasSuggestion) {
        Label assignee = new Label(getLoginName());
        assignee.setGraphic(getAvatarImageView());
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = fontLoader.computeStringWidth(assignee.getText(), assignee.getFont());
        assignee.setPrefWidth(width + 35 + AVATAR_SIZE);
        assignee.setPrefHeight(LABEL_HEIGHT);
        assignee.getStyleClass().add("labels");
        assignee.setStyle("-fx-background-color: yellow;");

        if (hasSuggestion) {
            assignee.setStyle(assignee.getStyle() + "-fx-opacity: 40%;");
        }

        if (isSelected) {
            assignee.setStyle(assignee.getStyle() + "-fx-border-color: black;");
        }

        return assignee;
    }

    public Node getExistingAssigneeNode(boolean hasSuggestion) {
        Label assignee = new Label(getLoginName());
        assignee.setGraphic(getAvatarImageView());
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = fontLoader.computeStringWidth(assignee.getText(), assignee.getFont());
        assignee.setPrefWidth(width + 35 + AVATAR_SIZE);
        assignee.setPrefHeight(LABEL_HEIGHT);
        assignee.getStyleClass().add("labels");
        assignee.setStyle("-fx-background-color: yellow;");



        if (isSelected && (hasSuggestion || isHighlighted)) {
            assignee.setStyle(assignee.getStyle() + "-fx-opacity: 40%;");
        }

        if (!isExisting || !isSelected || hasSuggestion || isHighlighted) {
            assignee.getStyleClass().add("labels-removed"); // add strikethrough
        }

        return assignee;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setHighlighted(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }

    public boolean isHighlighted() {
        return this.isHighlighted;
    }

    public void setFaded(boolean isFaded) {
        this.isFaded = isFaded;
    }

    public boolean isFaded() {
        return this.isFaded;
    }
    public void setExisting(boolean isExisting) {
        this.isExisting = isExisting;
    }

    public boolean isExisting() {
        return this.isExisting;
    }

    public int compareTo(PickerAssignee other) {
        String thisLoginName = this.getLoginName().toLowerCase();
        String otherLoginName = other.getLoginName().toLowerCase();
        return thisLoginName.compareTo(otherLoginName);
    }

    private ImageView getAvatarImageView(){
        return new ImageView(new Image(getAvatarURL(), AVATAR_SIZE, AVATAR_SIZE, true, true, true));
    }

    /**
     * This isn't unnecessary as fields are added, but are not taken into account for equality.
     * @return
     */
    @Override
    @SuppressWarnings("PMD")
    public boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * This isn't unnecessary as fields are added, but are not taken into account for equality.
     * @return
     */
    @Override
    @SuppressWarnings("PMD")
    public int hashCode() {
        return super.hashCode();
    }
}
