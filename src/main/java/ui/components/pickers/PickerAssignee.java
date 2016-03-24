package ui.components.pickers;

import backend.resource.TurboUser;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class PickerAssignee extends TurboUser implements Comparable<PickerAssignee> {

    private static final int AVATAR_SIZE = 30;
    private static final int LABEL_HEIGHT = 40;

    private boolean isExisting = false;
    private boolean isSelected = false;
    // set to true so that all users are matching when the text field is empty
    private boolean isMatching = true;

    public PickerAssignee(TurboUser user) {
        super(user);
    }

    public PickerAssignee(PickerAssignee assignee) {
        this((TurboUser) assignee);
        setExisting(assignee.isExisting());
        setSelected(assignee.isSelected());
        setMatching(assignee.isMatching());
    }

    public Node getMatchingNode() {
        HBox assigneeBox = getAssigneeHBox();
        ImageView avatar = getAvatarImageView();
        Label assigneeLoginName = getAssigneeLabel();

        if (isSelected) {
            assigneeLoginName.setText(assigneeLoginName.getText() + " ✓");
            assigneeBox.setStyle(assigneeBox.getStyle() + "-fx-background-color: skyblue");
        }

        assigneeBox.getChildren().setAll(avatar, assigneeLoginName);

        return assigneeBox;
    }

    public Node getNewlyAssignedAssigneeNode() {
        return getAssigneeLabelWithAvatar();
    }

    public Node getExistingAssigneeNode(boolean hasSelected) {
        Label assignee = getAssigneeLabelWithAvatar();
        if (hasSelected || !isSelected()) {
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

    public void setMatching(boolean isMatching) {
        this.isMatching = isMatching;
    }

    public boolean isMatching() {
        return this.isMatching;
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
        if (getAvatarURL().isEmpty()) return new ImageView();
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

    private HBox getAssigneeHBox() {
        HBox assigneeBox = new HBox();
        assigneeBox.setSpacing(30);
        assigneeBox.setPadding(new Insets(0, 0, 0, 30));
        assigneeBox.setAlignment(Pos.CENTER_LEFT);
        assigneeBox.setPrefWidth(398);
        return assigneeBox;
    }

    private Label getAssigneeLabel() {
        Label assigneeLoginName = new Label(getLoginName());
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = fontLoader.computeStringWidth(assigneeLoginName.getText(), assigneeLoginName.getFont());
        assigneeLoginName.setPrefWidth(width + 35);
        assigneeLoginName.setPrefHeight(LABEL_HEIGHT);
        assigneeLoginName.getStyleClass().add("labels");
        return assigneeLoginName;
    }

    private Label getAssigneeLabelWithAvatar() {
        Label assignee = new Label(getLoginName());
        assignee.setGraphic(getAvatarImageView());
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = fontLoader.computeStringWidth(assignee.getText(), assignee.getFont());
        assignee.setPrefWidth(width + 35 + AVATAR_SIZE);
        assignee.setPrefHeight(LABEL_HEIGHT);
        assignee.getStyleClass().add("labels");
        assignee.setStyle("-fx-background-color: lightgreen;");
        return assignee;
    }
}
