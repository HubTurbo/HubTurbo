package ui.components.pickers;

import util.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class handles the state and logic of the MilestonePickerDialog,
 * which determine the result of the user's input
 */
public class MilestonePickerState {
    public static final int BEST_MATCHING_LIMIT = 5;
    private final List<PickerMilestone> currentMilestones;
    private final List<PickerMilestone> bestMatchingMilestones;

    public MilestonePickerState(List<PickerMilestone> milestones, String userInput) {
        currentMilestones = cloneList(milestones);
        bestMatchingMilestones = new ArrayList<>();
        processInput(userInput);
    }

    private List<PickerMilestone> cloneList(List<PickerMilestone> sourceList) {
        return sourceList.stream()
                .map(PickerMilestone::new)
                .collect(Collectors.toList());
    }

    private void processInput(String userInput) {
        determineBestMatchingMilestones(userInput);
        if (userInput.isEmpty()) return;
        toggleFirstMilestone(bestMatchingMilestones);
    }

    /**
     * Toggles the milestone that has milestoneName as its exact name
     * <p>
     * This method is case-sensitive
     *
     * @param milestoneName
     */
    public final void toggleExactMatchMilestone(String milestoneName) {
        Optional<PickerMilestone> exactMatchMilestone = getExactMatchMilestone(currentMilestones, milestoneName);
        exactMatchMilestone.ifPresent(milestone -> toggleMilestone(milestone));
    }

    /**
     * Toggles the first milestone of the list
     *
     * @param milestoneList
     */
    private final void toggleFirstMilestone(List<PickerMilestone> milestoneList) {
        Optional<PickerMilestone> firstMatchingMilestone = milestoneList.stream()
                .findFirst();
        firstMatchingMilestone.ifPresent(this::toggleMilestone);
    }

    /**
     * Changes the selection statuses of all milestones in the milestone list,
     * such that the given milestone is toggled, and the rest are not selected
     *
     * @param milestone
     */
    private void toggleMilestone(PickerMilestone milestone) {
        this.currentMilestones.stream()
                .forEach(listMilestone -> listMilestone.setSelected(listMilestone.equals(milestone)
                        && !listMilestone.isSelected()));
        this.bestMatchingMilestones.stream()
                .forEach(listMilestone -> listMilestone.setSelected(listMilestone.equals(milestone)
                        && !listMilestone.isSelected()));
    }

    public List<PickerMilestone> getCurrentMilestones() {
        return this.currentMilestones;
    }

    public List<PickerMilestone> getBestMatchingMilestones() {
        return bestMatchingMilestones;
    }

    private void determineBestMatchingMilestones(String userInput) {
        if (userInput.isEmpty()) {
            bestMatchingMilestones.addAll(currentMilestones);
            return;
        }
        List<PickerMilestone> matchingMilestones = getLimitedMatchingMilestones(userInput, BEST_MATCHING_LIMIT);
        System.out.println(matchingMilestones.toString());
        bestMatchingMilestones.addAll(matchingMilestones);
        if (bestMatchingMilestones.size() == BEST_MATCHING_LIMIT) return;
        bestMatchingMilestones.addAll(getLimitedSuggestedMilestones(userInput, bestMatchingMilestones,
                BEST_MATCHING_LIMIT - bestMatchingMilestones.size()));
    }

    private List<PickerMilestone> getLimitedSuggestedMilestones(String userInput,
                                                                List<PickerMilestone> matchingMilestones,
                                                                int noToAdd) {
        List<PickerMilestone> suggestedMilestones = new ArrayList<>();
        suggestedMilestones.addAll(getLimitedMatchesFromPreviousInput(userInput, matchingMilestones, noToAdd));

        List<PickerMilestone> curMilestones = new ArrayList<>(matchingMilestones);
        curMilestones.addAll(suggestedMilestones);

        suggestedMilestones.addAll(getLimitedSortedMilestones(curMilestones,
                                                              noToAdd - suggestedMilestones.size()));

        suggestedMilestones.stream()
                .forEach(milestone -> milestone.setMatching(false));
        return suggestedMilestones;
    }

    /**
     * Gets first noToAdd milestones from currentMilestones which are not already in the result list
     * @param resultList
     * @param noToAdd
     * @return
     */
    private List<PickerMilestone> getLimitedSortedMilestones(List<PickerMilestone> resultList, int noToAdd) {
        return currentMilestones.stream()
                .filter(milestone -> !resultList.contains(milestone))
                .limit(noToAdd)
                .collect(Collectors.toList());
    }

    /**
     * Concatenates all the results from previous userInput(s)
     * Previous userInput(s) refer to userInput as it gets shorter after repetitively removing the last char
     * @param userInput
     * @param curList
     * @param noToAdd
     */
    private List<PickerMilestone> getLimitedMatchesFromPreviousInput(String userInput,
                                                                     List<PickerMilestone> curList, int noToAdd) {
        List<PickerMilestone> bestMatchesForPreviousInput = new ArrayList<>();
        String curUserInput = userInput;
        while (bestMatchesForPreviousInput.size() < noToAdd && curUserInput.length() > 0) {

            List<PickerMilestone> totalList = new ArrayList<>(curList);
            totalList.addAll(bestMatchesForPreviousInput);
            bestMatchesForPreviousInput.addAll(getNewMatchingMilestones(curUserInput, totalList));

            curUserInput = curUserInput.substring(0, curUserInput.length() - 1);
        }

        return bestMatchesForPreviousInput.stream()
                .limit(noToAdd)
                .collect(Collectors.toList());
    }

    /**
     * Filters currentMilestones based on the words of userInput
     *
     * @param userInput
     * @param limit
     * @return
     */
    private List<PickerMilestone> getLimitedMatchingMilestones(String userInput, int limit) {
        String[] userInputWords = userInput.split(" ");

        List<PickerMilestone> matchingMilestones = cloneList(currentMilestones);
        for (String word : userInputWords) {
            matchingMilestones = matchingMilestones.stream()
                    .filter(milestone -> isMatchingQuery(milestone, word))
                    .collect(Collectors.toList());
        }
        return matchingMilestones.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<PickerMilestone> getNewMatchingMilestones(String curUserInput, List<PickerMilestone> curList) {
        String[] curUserInputWords = curUserInput.split(" ");

        List<PickerMilestone> matchingMilestones = cloneList(currentMilestones);
        for (String word : curUserInputWords) {
            matchingMilestones = matchingMilestones.stream()
                    .filter(milestone -> isMatchingQuery(milestone, word))
                    .collect(Collectors.toList());
        }
        return matchingMilestones.stream()
                .filter(milestone -> !curList.contains(milestone))
                .collect(Collectors.toList());
    }

    private boolean isMatchingQuery(PickerMilestone milestone, String query) {
        return Utility.containsIgnoreCase(milestone.getTitle(), query);
    }

    private Optional<PickerMilestone> getExactMatchMilestone(List<PickerMilestone> milestoneList, String query) {
        return milestoneList.stream()
                .filter(milestone -> milestone.getTitle().equals(query))
                .findFirst();
    }
}
