package ui.components.pickers;

import util.Utility;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class handles the state and logic of the MilestonePickerDialog,
 * which determine the result of the user's input
 */
public class MilestonePickerState {
    private List<PickerMilestone> currentMilestonesList;

    public MilestonePickerState(List<PickerMilestone> milestones) {
        currentMilestonesList = cloneList(milestones);
    }

    public MilestonePickerState(List<PickerMilestone> milestones, String userInput) {
        this(milestones);
        processInput(userInput);
    }

    private List<PickerMilestone> cloneList(List<PickerMilestone> sourceList) {
        return sourceList.stream()
                .map(PickerMilestone::new)
                .collect(Collectors.toList());
    }

    private void processInput(String userInput) {
        if (userInput.isEmpty()) return;

        String[] userInputWords = userInput.split(" ");
        Stream.of(userInputWords)
                .forEach(this::filterMilestones);

        toggleFirstMatchingMilestone(currentMilestonesList);
    }

    /**
     * Toggles the milestone that has milestoneName as its exact name
     * <p>
     * This method is case-sensitive
     *
     * @param milestoneName
     */
    public final void toggleExactMatchMilestone(String milestoneName) {
        Optional<PickerMilestone> onlyMatchingMilestone = getExactMatchMilestone(currentMilestonesList, milestoneName);
        onlyMatchingMilestone.ifPresent(milestone -> toggleMilestone(milestone));
    }

    /**
     * Toggles the milestone that is the first match to the given query
     * <p>
     * This method is NOT case-sensitive
     *
     * @param milestoneList
     */
    private final void toggleFirstMatchingMilestone(List<PickerMilestone> milestoneList) {
        Optional<PickerMilestone> firstMatchingMilestone = getFirstMatchingMilestone(milestoneList);
        firstMatchingMilestone.ifPresent(this::toggleMilestone);
    }

    /**
     * Changes the selection statuses of all milestones in the milestone list,
     * such that the new given milestone is toggled, and the rest are not selected
     *
     * @param milestone
     */
    private void toggleMilestone(PickerMilestone milestone) {
        this.currentMilestonesList.stream()
                .forEach(listMilestone ->
                        listMilestone.setSelected(listMilestone.equals(milestone) && !listMilestone.isSelected()));
    }

    public List<PickerMilestone> getCurrentMilestonesList() {
        return this.currentMilestonesList;
    }

    /**
     * Gets the list of milestones that matches the current query
     *
     * @return
     */
    public List<PickerMilestone> getMatchingMilestonesList() {
        return this.currentMilestonesList.stream()
                .filter(milestone -> !milestone.isMatching())
                .collect(Collectors.toList());
    }

    private void filterMilestones(String query) {
        currentMilestonesList
                .forEach(milestone -> {
                    boolean matchQuery = Utility.containsIgnoreCase(milestone.getTitle(), query);
                    if (!milestone.isMatching()) milestone.setMatching(!matchQuery);
                });
    }

    private Optional<PickerMilestone> getFirstMatchingMilestone(List<PickerMilestone> milestoneList) {
        return milestoneList.stream()
                .filter(milestone -> !milestone.isMatching())
                .findFirst();
    }

    private Optional<PickerMilestone> getExactMatchMilestone(List<PickerMilestone> milestoneList, String query) {
        return milestoneList.stream()
                .filter(milestone -> milestone.getTitle().equals(query))
                .findFirst();
    }
}
