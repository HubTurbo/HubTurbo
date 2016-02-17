package ui.components.pickers;

import util.Utility;

import java.util.ArrayList;
import java.util.List;

public class MilestonePickerState {
    private List<PickerMilestone> originalMilestonesList;
    private List<PickerMilestone> currentMilestonesList;

    public MilestonePickerState(List<PickerMilestone> milestones) {
        originalMilestonesList = milestones;
        currentMilestonesList = new ArrayList<>();
        cloneList(originalMilestonesList, currentMilestonesList);
    }

    private void cloneList(List<PickerMilestone> sourceList, List<PickerMilestone> destList) {
        sourceList.stream()
                .forEach(milestone -> {
                    destList.add(new PickerMilestone(milestone));
                });
    }

    public void processInput(String userInput) {
        if (userInput.isEmpty()) {
            return;
        }

        String[] userInputWords = userInput.split(" ");
        for (int i = 0; i < userInputWords.length; i++) {
            String currentWord = userInputWords[i];
            if (i < userInputWords.length - 1 || userInput.endsWith(" ")) {
                toggleMilestone(currentWord);
            } else {
                filterMilestone(currentWord);
            }
        }
    }

    /**
     * Finds the PickerMilestone in the milestoneToDisplay list which has milestoneQuery in title,
     * then toggles the selection status
     * @param milestoneQuery
     */
    public void toggleMilestone(String milestoneQuery) {
        String milestoneName = getMilestoneName(milestoneQuery);
        if (milestoneName == null) return;
        this.currentMilestonesList.stream()
                .forEach(milestone -> {
                    milestone.setSelected(milestone.getTitle().equals(milestoneName)
                            && !milestone.isSelected());
                });
    }

    public void filterMilestone(String query) {
        currentMilestonesList.stream()
                .forEach(milestone -> {
                    boolean matchQuery = Utility.containsIgnoreCase(milestone.getTitle(), query);
                    milestone.setFaded(!matchQuery);
                });

        highlightFirstMatchingMilestone();
    }

    public List<PickerMilestone> getCurrentMilestonesList() {
        return this.currentMilestonesList;
    }

    private void highlightFirstMatchingMilestone() {
        if (hasMatchingMilestone(this.currentMilestonesList)) {
            this.currentMilestonesList.stream()
                    .filter(milestone -> !milestone.isFaded())
                    .findAny()
                    .get()
                    .setHighlighted(true);
        }
    }

    private boolean hasMatchingMilestone(List<PickerMilestone> milestoneList) {
        return milestoneList.stream()
                .filter(milestone -> !milestone.isFaded())
                .findAny()
                .isPresent();
    }

    private String getMilestoneName(String query) {
        if (hasExactlyOneMatchingMilestone(currentMilestonesList, query)) {
            return getMatchingMilestoneName(currentMilestonesList, query);
        }
        return null;
    }


    private boolean hasExactlyOneMatchingMilestone(List<PickerMilestone> milestoneList, String query) {
        return milestoneList.stream()
                .filter(milestone -> Utility.containsIgnoreCase(milestone.getTitle(), query))
                .count() == 1;
    }

    private String getMatchingMilestoneName(List<PickerMilestone> milestoneList, String query) {
        return milestoneList.stream()
                .filter(milestone -> Utility.containsIgnoreCase(milestone.getTitle(), query))
                .findFirst()
                .get()
                .getTitle();
    }
}
