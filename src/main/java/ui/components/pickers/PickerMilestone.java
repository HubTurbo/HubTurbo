package ui.components.pickers;

import backend.resource.TurboMilestone;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

public class PickerMilestone extends TurboMilestone implements Comparable<PickerMilestone> {
    public static final String OPEN_COLOUR = "#84BE54";
    public static final String CLOSE_COLOUR = "#AD3E27";
    private static final int SMALL_LABEL_FONT = 12;
    private static final int BIG_LABEL_FONT = 16;
    boolean isSelected = false;
    boolean isHighlighted = false;
    boolean isFaded = false;
    boolean isExisting = false;

    public PickerMilestone(TurboMilestone milestone) {
        super(milestone.getRepoId(), milestone.getId(), milestone.getTitle());
        setDueDate(milestone.getDueDate());
        setDescription(milestone.getDescription());
        setOpen(milestone.isOpen());
        setOpenIssues(milestone.getOpenIssues());
        setClosedIssues(milestone.getClosedIssues());
    }

    public PickerMilestone(PickerMilestone milestone) {
        this((TurboMilestone) milestone);
        setFaded(milestone.isFaded());
        setHighlighted(milestone.isHighlighted());
        setSelected(milestone.isSelected());
        setExisting(milestone.isExisting());
    }

    public Node getSimpleNode() {
        Label milestone = createCustomLabel(SMALL_LABEL_FONT);
        setOpenStatusColour(milestone);
        setRemovedInUI(milestone);
        return milestone;
    }

    public Node getNode() {
        Label milestone = createLabel();
        setOpenStatusColour(milestone);

        if (isSelected) setSelectedInUI(milestone);
        if (isHighlighted) setHighlightedInUI(milestone);
        if (isFaded) setFadedInUI(milestone);

        return milestone;
    }

    private Label createLabel() {
        Label milestone = new Label(getTitle());
        adjustWidthToFont(milestone);
        return milestone;
    }

    private void adjustWidthToFont(Label milestone) {
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = fontLoader.computeStringWidth(milestone.getText(), milestone.getFont());
        milestone.setPrefWidth(width + 30);
        milestone.getStyleClass().add("labels");
    }

    private Label createCustomLabel(int fontSize) {
        Label milestone = new Label(getTitle());
        milestone.setFont(new Font(fontSize));
        adjustWidthToFont(milestone);
        return milestone;
    }

    private void setOpenStatusColour(Label milestone) {
        milestone.setStyle("-fx-background-color: " + (isOpen() ? OPEN_COLOUR : CLOSE_COLOUR) + ";");
    }

    private void setFadedInUI(Label milestone) {
        milestone.setStyle(milestone.getStyle() + "-fx-opacity: 40%;");
    }

    private void setHighlightedInUI(Label milestone) {
        milestone.setStyle(milestone.getStyle() + "-fx-border-color: black;");
    }

    private void setSelectedInUI(Label milestone) {
        milestone.setText(milestone.getText() + " ✓");
    }

    private void setRemovedInUI(Label milestone) {
        milestone.getStyleClass().add("labels-removed"); // add strikethrough
    }

    public Node getNewlyAssignedMilestoneNode(boolean hasSuggestion) {
        Label milestone = createCustomLabel(BIG_LABEL_FONT);
        setOpenStatusColour(milestone);

        if (hasSuggestion) {
            setFadedInUI(milestone);
            if (isSelected) setRemovedInUI(milestone);
        }

        if (isSelected) {
            setHighlightedInUI(milestone);
        }

        return milestone;
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

    @Override
    public int compareTo(PickerMilestone milestone) {
        // highlighted milestones are smaller
        if (isHighlighted != milestone.isHighlighted()) {
            return isHighlighted ? -1 : 1;
        }
        // open milestones are smaller
        if (this.isOpen() != milestone.isOpen()) {
            return this.isOpen() ? -1 : 1;
        }

        if (this.getDueDate().equals(milestone.getDueDate())) return 0;

        // milestones with due dates are smaller
        if (!this.getDueDate().isPresent()) return 1;
        if (!milestone.getDueDate().isPresent()) return -1;

        // milestones with earlier due dates are smaller
        return this.getDueDate().get()
                .isBefore(milestone.getDueDate().get()) ? -1 : 1;
    }

    @Override
    @SuppressWarnings("PMD")
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    @SuppressWarnings("PMD")
    public int hashCode() {
        return super.hashCode();
    }

}
