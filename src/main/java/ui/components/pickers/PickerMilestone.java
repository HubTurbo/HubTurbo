package ui.components.pickers;

import backend.resource.TurboMilestone;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private boolean isExisting = false;
    private boolean isMatching = true;

    private double progress = 0;

    public PickerMilestone(TurboMilestone milestone) {
        super(milestone.getRepoId(), milestone.getId(), milestone.getTitle());
        setDueDate(milestone.getDueDate());
        setDescription(milestone.getDescription());
        setOpen(milestone.isOpen());
        setOpenIssues(milestone.getOpenIssues());
        setClosedIssues(milestone.getClosedIssues());
        this.progress = calculateProgress(milestone.getOpenIssues(), milestone.getClosedIssues());
    }

    public PickerMilestone(PickerMilestone milestone) {
        this((TurboMilestone) milestone);
        setSelected(milestone.isSelected());
        setExisting(milestone.isExisting());
        setMatching(milestone.isMatching());
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

    public Node getDetailedMilestoneNode() {
        HBox matchingMilestone = new HBox();
        matchingMilestone.setSpacing(3);
        matchingMilestone.setPadding(new Insets(3, 3, 3, 3));
        matchingMilestone.setStyle("-fx-border-width: 0 0 1 0; -fx-border-color: lightgrey;");

        HBox milestoneNodeBox = createMilestoneNodeBox();
        HBox milestoneDetailsBox = createMilestoneDetailsBox();

        matchingMilestone.getChildren().setAll(milestoneNodeBox, milestoneDetailsBox);
        return matchingMilestone;
    }

    private double calculateProgress(int openIssues, int closedIssues) {
        int totalIssues = openIssues + closedIssues;
        return totalIssues > 0 ? (double) closedIssues / totalIssues : 0;
    }

    private HBox createMilestoneDetailsBox() {
        HBox milestoneDetailsBox = new HBox();
        milestoneDetailsBox.setSpacing(3);
        milestoneDetailsBox.setPrefWidth(250);
        milestoneDetailsBox.setAlignment(Pos.CENTER_RIGHT);

        if (getDueDate().isPresent()) {
            Label dueDate = new Label(getDueDate().get().toString());
            dueDate.setPrefWidth(150);
            milestoneDetailsBox.getChildren().add(dueDate);
        }

        MilestoneProgressBar progressBar = new MilestoneProgressBar(getProgress());
        Label progressLabel = new Label(String.format("%3.0f%%", getProgress() * 100));
        progressLabel.setPrefWidth(50);
        milestoneDetailsBox.getChildren().addAll(progressBar, progressLabel);
        return milestoneDetailsBox;
    }

    private HBox createMilestoneNodeBox() {
        HBox milestoneNodeBox = new HBox();
        milestoneNodeBox.setPrefWidth(129);
        milestoneNodeBox.setAlignment(Pos.CENTER);
        milestoneNodeBox.getChildren().add(createMilestoneNode());
        return milestoneNodeBox;
    }

    private Node createMilestoneNode() {
        Label milestone = createLabel();
        setStatusColour(milestone);
        if (isSelected) setSelectedInUI(milestone);
        if (!isMatching) setFadedInUI(milestone);
        adjustWidthToFont(milestone);
        return milestone;
    }
    
    private Label createLabel() {
        return new Label(getTitle());
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
        milestone.setPrefWidth(width + 15);
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
     * Returns milestones that the user is allowed to select.
     * A milestone is allowed for selection if it is not an existing milestone.
     *
     * @param milestones
     */
    public static List<PickerMilestone> getSelectableMilestones(List<PickerMilestone> milestones) {
        return milestones.stream()
                .filter(milestone -> !milestone.isExisting())
                .collect(Collectors.toList());
    }

    private void setFadedInUI(Label milestone) {
        milestone.setStyle(milestone.getStyle() + " -fx-opacity: 60%;");
    }

    public void setMatching(boolean isMatching) {
        this.isMatching = isMatching;
    }

    public boolean isMatching() {
        return isMatching;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setExisting(boolean isExisting) {
        this.isExisting = isExisting;
    }

    public boolean isExisting() {
        return this.isExisting;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public double getProgress() {
        return this.progress;
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
