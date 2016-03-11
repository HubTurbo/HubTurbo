package ui.components.pickers;

import backend.resource.TurboMilestone;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class PickerMilestone extends TurboMilestone implements Comparable<PickerMilestone> {
    public static final String OPEN_COLOUR = "#84BE54";
    public static final String CLOSE_COLOUR = "#AD3E27";
    boolean isSelected = false;
    boolean isHighlighted = false;
    boolean isFaded = false;
    boolean isExisting = false;

    public PickerMilestone(TurboMilestone milestone) {
        super(milestone.getRepoId(), milestone.getId(), milestone.getTitle());
        setDueDate(milestone.getDueDate());
        setDescription(milestone.getDescription() == null ? "" : milestone.getDescription());
        setOpen(milestone.isOpen());
        setOpenIssues(milestone.getOpenIssues());
        setClosedIssues(milestone.getClosedIssues());
    }

    public PickerMilestone(PickerMilestone milestone) {
        this((TurboMilestone)milestone);
        setFaded(milestone.isFaded());
        setHighlighted(milestone.isHighlighted());
        setSelected(milestone.isSelected());
        setExisting(milestone.isExisting());
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
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = fontLoader.computeStringWidth(milestone.getText(), milestone.getFont());
        milestone.setPrefWidth(width + 30);
        milestone.getStyleClass().add("labels");
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
        Label milestone = createLabel();
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

    public Node getExistingMilestoneNode(boolean hasSuggestion) {
        Label milestone = createLabel();
        setOpenStatusColour(milestone);

        if (isSelected && (hasSuggestion || isHighlighted)) {
            setFadedInUI(milestone);
        }

        if (!isExisting || !isSelected || hasSuggestion || isHighlighted) {
            setRemovedInUI(milestone);
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

    /**
     * This treats null milestones with no set due date to be "larger than"
     * those which have due dates
     *
     * @param milestone
     * @return
     */
    @Override
    public int compareTo(PickerMilestone milestone) {
        if (this.getDueDate().equals(milestone.getDueDate())) return 0;
        if (!this.getDueDate().isPresent()) return 1;
        if (!milestone.getDueDate().isPresent()) return -1;

        return this.getDueDate().get()
                .isAfter(milestone.getDueDate().get()) ? 1 : -1;
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
