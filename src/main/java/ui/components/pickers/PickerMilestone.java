package ui.components.pickers;

import backend.resource.TurboMilestone;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

import java.util.List;
import java.util.Optional;

/**
 * This class handles the statuses and appearance of the milestones in MilestonePickerDialog
 */
public class PickerMilestone extends TurboMilestone implements Comparable<PickerMilestone> {

    public static final String OPEN_COLOUR = "#A5EEA5";
    public static final String CLOSED_COLOUR = "#FCA6B0";
    public static final String OVERDUE_COLOUR = "#FFD76E";

    private static final int SMALL_LABEL_FONT = 12;
    private static final int BIG_LABEL_FONT = 16;

    private boolean isSelected = false;
    private boolean isMatching = false;
    private boolean isExisting = false;

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
        setMatching(milestone.isMatching());
        setSelected(milestone.isSelected());
        setExisting(milestone.isExisting());
    }

    public Node getNode() {
        Label milestone = createLabel();
        setStatusColour(milestone);
        if (isSelected) setSelectedInUI(milestone);
        return milestone;
    }

    public Node getExistingMilestoneNode() {
        Label milestone = createCustomLabel(SMALL_LABEL_FONT);
        setStatusColour(milestone);
        setRemovedInUI(milestone);
        return milestone;
    }

    public Node getNewlyAssignedMilestoneNode() {
        Label milestone = createCustomLabel(BIG_LABEL_FONT);
        setStatusColour(milestone);
        if (isSelected) setHighlightedInUI(milestone);
        return milestone;
    }

    private Label createLabel() {
        Label milestone = new Label(getTitle());
        adjustWidthToFont(milestone);
        return milestone;
    }

    private Label createCustomLabel(int fontSize) {
        Label milestone = new Label(getTitle());
        milestone.setFont(new Font(fontSize));
        adjustWidthToFont(milestone);
        return milestone;
    }

    private void adjustWidthToFont(Label milestone) {
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = fontLoader.computeStringWidth(milestone.getText(), milestone.getFont());
        milestone.setPrefWidth(width + 30);
        milestone.getStyleClass().add("labels");
    }

    private void setStatusColour(Label milestone) {
        String colour = isOpen() ? (isOverdue() ? OVERDUE_COLOUR : OPEN_COLOUR) : CLOSED_COLOUR;
        milestone.setStyle("-fx-background-color: " + colour + ";");
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

    /**
     * Gets the existing milestone from the milestoneList
     *
     * @param milestoneList
     * @return Optional of existing milestone
     */
    public static Optional<PickerMilestone> getExistingMilestone(List<PickerMilestone> milestoneList) {
        return milestoneList.stream()
                .filter(PickerMilestone::isExisting)
                .findAny();
    }

    /**
     * Gets the selected milestone from the milestoneList
     *
     * @param milestoneList
     * @return Optional of selected milestone
     */
    public static Optional<PickerMilestone> getSelectedMilestone(List<PickerMilestone> milestoneList) {
        return milestoneList.stream()
                .filter(PickerMilestone::isSelected)
                .findAny();
    }

    /**
     * Gets the default milestone from the sortedMilestoneList
     * If there is an existing milestone, default milestone is the existing milestone
     * Else it is the first open milestone that is not overdue
     * Precondition: sortedMilestoneList needs to be sorted in its natural order
     *
     * @param sortedMilestoneList
     * @return Optional of default milestone
     */
    public static Optional<PickerMilestone> getDefaultMilestone(List<PickerMilestone> sortedMilestoneList) {
        return PickerMilestone.getExistingMilestone(sortedMilestoneList)
                .map(Optional::of)
                .orElse(PickerMilestone.getNextOpenMilestone(sortedMilestoneList));

    }

    /**
     * Gets the the first PickerMilestone that is open and not overdue from the sortedMilestoneList
     * Precondition: sortedMilestoneList needs to be sorted in its natural order
     *
     * @param sortedMilestoneList
     * @return Optional of first PickerMilestone that is open and not overdue
     */
    private static Optional<PickerMilestone> getNextOpenMilestone(List<PickerMilestone> sortedMilestoneList) {
        return sortedMilestoneList.stream()
                .filter(milestone -> !milestone.isOverdue() && milestone.isOpen())
                .findFirst();
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setMatching(boolean isFaded) {
        this.isMatching = isFaded;
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

    @Override
    public int compareTo(PickerMilestone milestone) {
        // selected milestones are smaller
        if (isSelected != milestone.isSelected()) {
            return isSelected ? -1 : 1;
        }
        // open milestones are smaller
        if (this.isOpen() != milestone.isOpen()) {
            return this.isOpen() ? -1 : 1;
        }

        // milestones ordered according to their titles if due dates are the same
        if (this.getDueDate().equals(milestone.getDueDate())) {
            return this.getTitle().compareTo(milestone.getTitle());
        }

        // milestones with due dates are smaller
        if (!this.getDueDate().isPresent()) return 1;
        if (!milestone.getDueDate().isPresent()) return -1;

        // milestones with earlier due dates are smaller
        return this.getDueDate().get().isBefore(milestone.getDueDate().get()) ? -1 : 1;
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
