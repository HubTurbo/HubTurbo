package ui.components.pickers;

import util.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MilestonePickerState {
    private List<PickerMilestone> currentMilestonesList;

    public MilestonePickerState(List<PickerMilestone> milestones) {
        currentMilestonesList = new ArrayList<>();
        cloneList(milestones, currentMilestonesList);
    }

    public MilestonePickerState(List<PickerMilestone> milestones, String userInput) {
        this(milestones);
        processInput(userInput);
    }

    private void cloneList(List<PickerMilestone> sourceList, List<PickerMilestone> destList) {
        sourceList.stream()
                .forEach(milestone -> destList.add(new PickerMilestone(milestone)));
    }

    private void processInput(String userInput) {
        if (userInput.isEmpty()) return;

        String[] userInputWords = userInput.split(" ");
        for (int i = 0; i < userInputWords.length; i++) {
            String currentWord = userInputWords[i];
            if (i < userInputWords.length - 1 || userInput.endsWith(" ")) {
                toggleMilestone(currentWord);
            } else {
                filterMilestones(currentWord);
            }
        }
    }

    /**
     * Gets a milestone that is the ONLY match to the given milestoneQuery
     * Changes the selection statuses of all milestones in the milestone list,
     * such that the new given milestone is toggled, and the rest are not selected
     * @param milestoneQuery
     */
    public final void toggleMilestone(String milestoneQuery) {
        Optional<PickerMilestone> onlyMatchingMilestone = getMatchingMilestoneIfOnlyMatch(milestoneQuery);
        if (!onlyMatchingMilestone.isPresent()) return;
        this.currentMilestonesList.stream()
                .forEach(milestone -> milestone.setSelected(milestone.equals(onlyMatchingMilestone.get())
                        && !milestone.isSelected()));
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

        if (hasExactlyOneMatchingMilestone(currentMilestonesList, query)) highlightAnyMatchingMilestone();
    }

    private void highlightAnyMatchingMilestone() {
        if (!hasMatchingMilestone(this.currentMilestonesList)) return;
        this.currentMilestonesList.stream()
                .filter(milestone -> !milestone.isFaded())
                .findAny()
                .get()
                .setHighlighted(true);
    }

    private boolean hasMatchingMilestone(List<PickerMilestone> milestoneList) {
        return milestoneList.stream()
                .filter(milestone -> !milestone.isFaded())
                .findAny()
                .isPresent();
    }

    /**
     * Gets the name of milestone if it is the only matching milestone to the query
     * @param query
     * @return
     */
    private Optional<PickerMilestone> getMatchingMilestoneIfOnlyMatch(String query) {
        if (!hasExactlyOneMatchingMilestone(currentMilestonesList, query)) return Optional.empty();

        return Optional.of(getMatchingMilestone(currentMilestonesList, query));
    }

    private boolean hasExactlyOneMatchingMilestone(List<PickerMilestone> milestoneList, String query) {
        return milestoneList.stream()
                .filter(milestone -> Utility.containsIgnoreCase(milestone.getTitle(), query))
                .count() == 1;
    }

    private PickerMilestone getMatchingMilestone(List<PickerMilestone> milestoneList, String query) {
        return milestoneList.stream()
                .filter(milestone -> Utility.containsIgnoreCase(milestone.getTitle(), query))
                .findFirst()
                .get();
    }
}
