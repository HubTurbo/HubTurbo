package ui.components.pickers;

import util.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        List<PickerMilestone> destList = new ArrayList<>();
        sourceList.stream()
                .forEach(milestone -> destList.add(new PickerMilestone(milestone)));
        return destList;
    }

    private void processInput(String userInput) {
        if (userInput.isEmpty()) return;

        String[] userInputWords = userInput.split(" ");
        for (int i = 0; i < userInputWords.length; i++) {
            String currentWord = userInputWords[i];
            if (isConfirmedKeyword(userInput, userInputWords, i)) {
                toggleFirstMatchingMilestone(currentWord);
            } else {
                filterMilestones(currentWord);
            }
        }
    }

    /**
     * Checks if the ith word of the userInput is confirmed i.e. has a space after it
     * @param userInput
     * @param userInputWords
     * @param i
     * @return
     */
    private boolean isConfirmedKeyword(String userInput, String[] userInputWords, int i) {
        return i < userInputWords.length - 1 || userInput.endsWith(" ");
    }

    /**
     * Toggles the milestone that has milestoneName as its exact name
     *
     * This method is case-sensitive
     * @param milestoneName
     */
    public final void toggleExactMatchMilestone(String milestoneName) {
        Optional<PickerMilestone> onlyMatchingMilestone = getExactMatchMilestone(currentMilestonesList, milestoneName);
        onlyMatchingMilestone.ifPresent(milestone -> toggleMilestone(milestone));
    }

    /**
     * Toggles the milestone that is the first match to the given query
     *
     * This method is NOT case-sensitive
     * @param query
     */
    private final void toggleFirstMatchingMilestone(String query) {
        Optional<PickerMilestone> firstMatchingMilestone = getFirstMatchingMilestone(currentMilestonesList, query);
        firstMatchingMilestone.ifPresent(milestone -> toggleMilestone(milestone));
    }

    /**
     * Changes the selection statuses of all milestones in the milestone list,
     * such that the new given milestone is toggled, and the rest are not selected
     *
     * @param milestone
     */
    private void toggleMilestone(PickerMilestone milestone) {
        this.currentMilestonesList.stream()
                .forEach(listMilestone -> listMilestone.setSelected(listMilestone.equals(milestone)
                        && !listMilestone.isSelected()));
    }

    /**
     * Gets the current list of milestones
     * @return
     */
    public List<PickerMilestone> getCurrentMilestonesList() {
        return this.currentMilestonesList;
    }

    /**
     * Gets the list of milestones that matches the current query i.e. not faded
     * @return
     */
    public List<PickerMilestone> getMatchingMilestonesList() {
        return this.currentMilestonesList.stream()
                .filter(milestone -> !milestone.isFaded())
                .collect(Collectors.toList());
    }

    private void filterMilestones(String query) {
        currentMilestonesList.stream()
                .forEach(milestone -> {
                    boolean matchQuery = Utility.containsIgnoreCase(milestone.getTitle(), query);
                    milestone.setFaded(!matchQuery);
                });

        if (hasMatchingMilestone(currentMilestonesList)) highlightFirstMatchingMilestone();
    }

    private void highlightFirstMatchingMilestone() {
        if (!hasMatchingMilestone(this.currentMilestonesList)) return;
        this.currentMilestonesList.stream()
                .filter(milestone -> !milestone.isFaded())
                .findFirst()
                .get()
                .setHighlighted(true);
    }

    private boolean hasMatchingMilestone(List<PickerMilestone> milestoneList) {
        return milestoneList.stream()
                .filter(milestone -> !milestone.isFaded())
                .findAny()
                .isPresent();
    }

    private Optional<PickerMilestone> getFirstMatchingMilestone(List<PickerMilestone> milestoneList, String query) {
        return milestoneList.stream()
                .filter(milestone -> Utility.containsIgnoreCase(milestone.getTitle(), query))
                .findFirst();
    }

    private Optional<PickerMilestone> getExactMatchMilestone(List<PickerMilestone> milestoneList, String query) {
        return milestoneList.stream()
                .filter(milestone -> milestone.getTitle().equals(query))
                .findFirst();
    }
}
