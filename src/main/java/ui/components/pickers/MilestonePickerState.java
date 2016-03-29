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
        filterMilestonesByInput(currentMilestones, userInput);
        determineBestMatchingMilestones(userInput);
        if (userInput.isEmpty()) return;
        toggleFirstMilestone(bestMatchingMilestones);
    }

    private void filterMilestonesByInput(List<PickerMilestone> milestones, String userInput) {
        String[] userInputWords = userInput.split(" ");
        Stream.of(userInputWords)
                .forEach(query -> filterMilestonesByQuery(milestones, query));
    }

    private void filterMilestonesByQuery(List<PickerMilestone> milestones, String query) {
        milestones.stream()
                .forEach(milestone -> {
                    if (milestone.isMatching()) milestone.setMatching(isMatchingQuery(milestone, query));
                });
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
        exactMatchMilestone.ifPresent(this::toggleMilestone);
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
     * Changes the selection statuses of all milestones in the currentMilestones,
     * such that the given milestone is toggled, and the rest are not selected
     *
     * @param milestone
     */
    private void toggleMilestone(PickerMilestone milestone) {
        this.currentMilestones.stream()
                .forEach(listMilestone -> listMilestone.setSelected(listMilestone.equals(milestone)
                        && !listMilestone.isSelected()));
    }

    public List<PickerMilestone> getCurrentMilestones() {
        return currentMilestones;
    }

    public List<PickerMilestone> getBestMatchingMilestones() {
        return bestMatchingMilestones;
    }

    /**
     * Contains the algorithm to generate the best matching milestones
     * These milestones will be added to bestMatchingMilestones
     *
     * The added milestones are references to the actual elements in currentMilestones, and thus should not be
     * unnecessarily mutated
     * @param userInput
     */
    private void determineBestMatchingMilestones(String userInput) {
        addMatchingMilestones(bestMatchingMilestones, getNumberOfMilestonesToAdd(), currentMilestones);
        addPreviouslyMatchingMilestones(bestMatchingMilestones, getNumberOfMilestonesToAdd(), userInput,
                                        currentMilestones);
        addLikelyUnmatchedMilestones(bestMatchingMilestones, getNumberOfMilestonesToAdd(), currentMilestones);
    }

    private int getNumberOfMilestonesToAdd() {
        return BEST_MATCHING_LIMIT - bestMatchingMilestones.size();
    }

    /**
     * Adds up to {@code limit} number of milestones to {@code bestMatchingMilestones}
     * These milestones are taken from {@code milestones}, in the same order given,
     * if they are not already in {@code bestMatchingMilestones}
     *
     * This method will do nothing if {@code limit} is not a positive integer
     * @param bestMatchingMilestones
     * @param limit
     * @param milestones
     */
    private void addLikelyUnmatchedMilestones(List<PickerMilestone> bestMatchingMilestones, int limit,
                                              List<PickerMilestone> milestones) {
        if (limit <= 0) return;
        List<PickerMilestone> likelyUnmatchedMilestones = getLimitedFirstMilestones(bestMatchingMilestones,
                limit, milestones);
        bestMatchingMilestones.addAll(likelyUnmatchedMilestones);
    }

    /**
     * Adds up to {@code limit} number of milestones to {@code bestMatchingMilestones}
     * These milestones are taken from {@code milestones} if they match subsets of {@code userInput}, and are
     * not already in {@code bestMatchingMilestones}
     *
     * This method will do nothing if {@code limit} is not a positive integer
     * @param bestMatchingMilestones
     * @param limit
     * @param userInput
     * @param milestones
     */
    private void addPreviouslyMatchingMilestones(List<PickerMilestone> bestMatchingMilestones, int limit,
                                                 String userInput, List<PickerMilestone> milestones) {
        if (limit <= 0) return;
        List<PickerMilestone> previouslyMatchingMilestones = getLimitedMatchesFromPreviousInput(userInput,
                bestMatchingMilestones, limit, milestones);
        bestMatchingMilestones.addAll(previouslyMatchingMilestones);
    }

    /**
     * Adds up to {@code limit} number of milestones to {@code bestMatchingMilestones}
     * These milestones are taken from {@code milestones} if its isMatching() returns true
     *
     * This method will do nothing if {@code limit} is not a positive integer
     * @param bestMatchingMilestones
     * @param limit
     * @param milestones
     */
    private void addMatchingMilestones(List<PickerMilestone> bestMatchingMilestones, int limit,
                                       List<PickerMilestone> milestones) {
        if (limit <= 0) return;
        List<PickerMilestone> matchingMilestones = getLimitedMatchingMilestones(limit, milestones);
        bestMatchingMilestones.addAll(matchingMilestones);
    }

    /**
     * Gets first {@code limit} milestones from {@code milestones} which are not already in {@code currentMilestones}
     *
     * Assumption: {@code limit} is not negative
     * @param currentMilestones
     * @param limit
     * @param milestones
     * @return
     */
    private List<PickerMilestone> getLimitedFirstMilestones(List<PickerMilestone> currentMilestones,
                                                            int limit, List<PickerMilestone> milestones) {
        return milestones.stream()
                .filter(milestone -> !currentMilestones.contains(milestone))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Concatenates all the matching milestones for previous userInput(s), then gets the first {@code limit}
     * milestones which are not already in {@code currentMilestones}
     *
     * Previous userInput(s) refer to userInput as it gets shorter after repetitively removing the last char
     * e.g. previous userInputs of "milestones" are "milestone", "mileston", "milesto", ..., and they will be
     * checked for matching milestones in that order
     *
     * Assumption: {@code limit} is not negative
     * @param userInput
     * @param currentMilestones
     * @param limit
     * @param milestones
     */
    private List<PickerMilestone> getLimitedMatchesFromPreviousInput(String userInput,
                                                                     List<PickerMilestone> currentMilestones,
                                                                     int limit,
                                                                     List<PickerMilestone> milestones) {
        List<PickerMilestone> bestMatchesForPreviousInput = new ArrayList<>();
        String curUserInput = userInput;
        while (bestMatchesForPreviousInput.size() < limit && curUserInput.length() > 0) {
            List<PickerMilestone> totalList = new ArrayList<>(currentMilestones);
            totalList.addAll(bestMatchesForPreviousInput);

            List<PickerMilestone> matchingMilestones = getNewMatchingMilestones(curUserInput, totalList, milestones);
            bestMatchesForPreviousInput.addAll(matchingMilestones);

            curUserInput = curUserInput.substring(0, curUserInput.length() - 1);
        }

        return bestMatchesForPreviousInput.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Gets up to {@code limit} number of milestones from {@code milestones} whose isMatching() returns true
     *
     * Assumption: {@code limit} is not negative
     * @param limit non-negative integer
     * @param milestones milestones to get the matching milestones from
     * @return
     */
    private List<PickerMilestone> getLimitedMatchingMilestones(int limit, List<PickerMilestone> milestones) {
        return milestones.stream()
                .filter(PickerMilestone::isMatching)
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Gets milestones from {@code milestones} which match {@code userInput},
     * and are not already included in {@code chosenMilestones}
     *
     * @param userInput
     * @param chosenMilestones
     * @param milestones
     * @return
     */
    private List<PickerMilestone> getNewMatchingMilestones(String userInput, List<PickerMilestone> chosenMilestones,
                                                           List<PickerMilestone> milestones) {
        String[] curUserInputWords = userInput.split(" ");

        List<PickerMilestone> matchingMilestones = milestones;
        for (String word : curUserInputWords) {
            matchingMilestones = matchingMilestones.stream()
                    .filter(milestone -> isMatchingQuery(milestone, word))
                    .collect(Collectors.toList());
        }
        return matchingMilestones.stream()
                .filter(milestone -> !chosenMilestones.contains(milestone))
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
