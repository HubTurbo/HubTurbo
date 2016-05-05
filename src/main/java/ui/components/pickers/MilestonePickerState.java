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
    private final List<PickerMilestone> allMilestones;
    private final List<PickerMilestone> bestMatchingMilestones;

    public MilestonePickerState(List<PickerMilestone> milestones, String userQuery) {
        allMilestones = cloneList(milestones);
        bestMatchingMilestones = new ArrayList<>();
        processQuery(userQuery);
    }

    public MilestonePickerState(List<PickerMilestone> milestones) {
        this(milestones, "");
    }

    private List<PickerMilestone> cloneList(List<PickerMilestone> sourceList) {
        return sourceList.stream()
                .map(PickerMilestone::new)
                .collect(Collectors.toList());
    }

    private void processQuery(String querySentence) {
        processMatchingMilestonesByQuerySentence(allMilestones, querySentence);
        populateBestMatchingMilestones(querySentence);
        if (querySentence.isEmpty()) return;
        toggleFirstMilestone(bestMatchingMilestones);
    }

    private void processMatchingMilestonesByQuerySentence(List<PickerMilestone> milestones, String querySentence) {
        String[] queryWords = querySentence.split(" ");
        Stream.of(queryWords)
                .forEach(queryWord -> processMatchingMilestonesByQueryWord(milestones, queryWord));
    }

    private void processMatchingMilestonesByQueryWord(List<PickerMilestone> milestones, String queryWord) {
        milestones.stream()
                .forEach(milestone -> {
                    if (milestone.isMatching()) milestone.setMatching(isMatchingQuery(milestone, queryWord));
                });
    }

    /**
     * Selects the next milestone in the list of best matching milestones
     *
     * If there is no currently selected milestone, the next milestone will be the first one in
     * the list
     */
    public void selectNextBestMatchingMilestone() {
        Optional<Integer> curSelectedIndex = getSelectedIndex(bestMatchingMilestones);
        if (!canIncreaseIndex(curSelectedIndex, bestMatchingMilestones)) return;
        selectMilestoneAtIndex(bestMatchingMilestones, curSelectedIndex.orElse(-1) + 1);
    }

    /**
     * Selects the previous milestone in the list of best matching milestones
     */
    public void selectPreviousBestMatchingMilestone() {
        Optional<Integer> curSelectedIndex = getSelectedIndex(bestMatchingMilestones);
        if (!canDecreaseIndex(curSelectedIndex)) return;
        selectMilestoneAtIndex(bestMatchingMilestones, curSelectedIndex.get() - 1);
    }

    private boolean canIncreaseIndex(Optional<Integer> curSelectedIndex, List<PickerMilestone> list) {
        return !curSelectedIndex.isPresent() || curSelectedIndex.get() < list.size() - 1;
    }

    private boolean canDecreaseIndex(Optional<Integer> curSelectedIndex) {
        return curSelectedIndex.isPresent() && curSelectedIndex.get() > 0;
    }

    /**
     * Returns the first index at which a milestone is selected
     *
     * @param milestones
     */
    private Optional<Integer> getSelectedIndex(List<PickerMilestone> milestones) {
        for (int i = 0; i < milestones.size(); i++) {
            if (milestones.get(i).isSelected()) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    /**
     * Selects the milestone at the given index and deselects the rest
     * @param milestones
     * @param index
     */
    private void selectMilestoneAtIndex(List<PickerMilestone> milestones, int index) {
        for (int i = 0; i < milestones.size(); i++) {
            milestones.get(i).setSelected(i == index);
        }
    }

    /**
     * Toggles the selection status of the milestone whose name is exactly milestoneName. This method is
     * case-sensitive
     * 
     * @param milestoneName
     */
    public final void toggleExactMatchMilestone(String milestoneName) {
        Optional<PickerMilestone> exactMatchMilestone = getExactMatchMilestone(allMilestones, milestoneName);
        exactMatchMilestone.ifPresent(this::toggleMilestone);
    }

    /**
     * Toggles the selection status of the first milestone in the list
     *
     * @param milestones
     */
    private final void toggleFirstMilestone(List<PickerMilestone> milestones) {
        Optional<PickerMilestone> firstMilestone = milestones.stream()
                                                            .findFirst();
        firstMilestone.ifPresent(this::toggleMilestone);
    }

    /**
     * Changes the selection statuses of all milestones in allMilestones,
     * such that the given milestone is toggled and the rest are not selected
     *
     * @param milestone
     */
    private void toggleMilestone(PickerMilestone milestone) {
        allMilestones.stream()
                .forEach(listMilestone -> listMilestone.setSelected(listMilestone.equals(milestone)
                        && !listMilestone.isSelected()));
    }

    public List<PickerMilestone> getAllMilestones() {
        return allMilestones;
    }

    public List<PickerMilestone> getBestMatchingMilestones() {
        return bestMatchingMilestones;
    }

    /**
     * Populates bestMatchingMilestones with milestones from allMilestones that best match
     * {@code querySentence} and are not already assigned.
     *
     * The added milestones are references to the actual elements in allMilestones, and thus should not be
     * unnecessarily mutated.
     * @param querySentence
     */
    private void populateBestMatchingMilestones(String querySentence) {
        bestMatchingMilestones.clear();
        List<PickerMilestone> selectableMilestones = PickerMilestone.getSelectableMilestones(allMilestones);

        addMatchingMilestones(bestMatchingMilestones, getRemainingBestMatchesToLimit(), selectableMilestones);
        if (isMilestonesSizeBelowLimit(bestMatchingMilestones, BEST_MATCHING_LIMIT)) {
            addPartiallyMatchingMilestones(bestMatchingMilestones, getRemainingBestMatchesToLimit(), querySentence,
                                           selectableMilestones);
        }
        if (isMilestonesSizeBelowLimit(bestMatchingMilestones, BEST_MATCHING_LIMIT)) {
            addLikelyUnmatchedMilestones(bestMatchingMilestones, getRemainingBestMatchesToLimit(),
                                         selectableMilestones);
        }
    }

    private boolean isMilestonesSizeBelowLimit(List<PickerMilestone> milestones, int limit) {
        return milestones.size() < limit;
    }

    private int getRemainingBestMatchesToLimit() {
        return BEST_MATCHING_LIMIT - bestMatchingMilestones.size();
    }

    /**
     * Adds a limited number of milestones to {@code milestonesToBePopulated}.
     * These milestones are taken from {@code allMilestones}, in the same order given,
     * if they are not already in {@code milestonesToBePopulated}.
     *
     * Assumption: {@code limit} is a positive integer
     * @param milestonesToBePopulated
     * @param limit
     * @param allMilestones
     */
    private void addLikelyUnmatchedMilestones(List<PickerMilestone> milestonesToBePopulated, int limit,
                                              List<PickerMilestone> allMilestones) {
        assert limit > 0;
        List<PickerMilestone> likelyUnmatchedMilestones =
                getLimitedFirstMilestones(milestonesToBePopulated, limit, allMilestones);
        milestonesToBePopulated.addAll(likelyUnmatchedMilestones);
    }

    /**
     * Adds a limited number of milestones to {@code milestonesToBePopulated}.
     * These milestones are taken from {@code allMilestones} if they match subsets of {@code querySentence} and are
     * not already in {@code milestonesToBePopulated}.
     *
     * Assumption: {@code limit} is a positive integer
     * @param milestonesToBePopulated
     * @param limit
     * @param querySentence
     * @param allMilestones
     */
    private void addPartiallyMatchingMilestones(List<PickerMilestone> milestonesToBePopulated, int limit,
                                                String querySentence, List<PickerMilestone> allMilestones) {
        assert limit > 0;
        List<PickerMilestone> partiallyMatchingMilestones =
                getLimitedMatchesFromPreviousQuery(querySentence, milestonesToBePopulated, limit, allMilestones);
        milestonesToBePopulated.addAll(partiallyMatchingMilestones);
    }

    /**
     * Adds a limited number of milestones to {@code milestonesToBePopulated}.
     * These milestones are taken from {@code allMilestones} if its isMatching() returns true.
     *
     * Assumption: {@code limit} is a positive integer
     * @param milestonesToBePopulated
     * @param limit
     * @param allMilestones
     */
    private void addMatchingMilestones(List<PickerMilestone> milestonesToBePopulated, int limit,
                                       List<PickerMilestone> allMilestones) {
        assert limit > 0;
        List<PickerMilestone> matchingMilestones = getLimitedMatchingMilestones(limit, allMilestones);
        milestonesToBePopulated.addAll(matchingMilestones);
    }

    /**
     * Gets a limited number of milestones from {@code allMilestones} matching previous querySentence(s)
     * which are not already in {@code currentMilestones}.
     *
     * Previous querySentence(s) refer to querySentence as it gets shorter after repetitively removing the last char
     * e.g. previous querySentences of "milestones" are "milestone", "mileston", "milesto", ..., and they will be
     * checked for matching milestones in that order
     *
     * Assumption: {@code limit} is not negative
     * @param querySentence
     * @param currentMilestones
     * @param limit
     * @param allMilestones
     */
    private List<PickerMilestone> getLimitedMatchesFromPreviousQuery(String querySentence,
                                                                     List<PickerMilestone> currentMilestones,
                                                                     int limit, List<PickerMilestone> allMilestones) {
        List<PickerMilestone> matchesForPreviousQuery = new ArrayList<>();
        String curQuerySentence = querySentence;
        while (matchesForPreviousQuery.size() < limit && curQuerySentence.length() > 0) {
            List<PickerMilestone> totalList = new ArrayList<>(currentMilestones);
            totalList.addAll(matchesForPreviousQuery);

            List<PickerMilestone> matchingMilestones = getNewMatchingMilestones(curQuerySentence, totalList,
                                                                                allMilestones);
            matchesForPreviousQuery.addAll(matchingMilestones);

            curQuerySentence = curQuerySentence.substring(0, curQuerySentence.length() - 1);
        }

        return matchesForPreviousQuery.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Gets milestones from {@code allMilestones} which match {@code querySentence} and are not already
     * included in {@code currentMilestones}
     *
     * @param querySentence
     * @param currentMilestones
     * @param allMilestones
     */
    private List<PickerMilestone> getNewMatchingMilestones(String querySentence,
                                                           List<PickerMilestone> currentMilestones,
                                                           List<PickerMilestone> allMilestones) {
        String[] curQueryWords = querySentence.split(" ");

        List<PickerMilestone> matchingMilestones = allMilestones;
        for (String word : curQueryWords) {
            matchingMilestones = matchingMilestones.stream()
                    .filter(milestone -> isMatchingQuery(milestone, word))
                    .collect(Collectors.toList());
        }
        return matchingMilestones.stream()
                .filter(milestone -> !currentMilestones.contains(milestone))
                .collect(Collectors.toList());
    }

    /**
     * Gets a limited number of milestones from {@code milestones} whose isMatching() returns true
     *
     * Assumption: {@code limit} is not negative
     * @param limit non-negative integer
     * @param milestones milestones to get the matching milestones from
     */
    private List<PickerMilestone> getLimitedMatchingMilestones(int limit, List<PickerMilestone> milestones) {
        return milestones.stream()
                .filter(PickerMilestone::isMatching)
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Gets the first {@code limit} milestones from {@code allMilestones} which are not already in
     * {@code currentMilestones}.
     *
     * Assumptions: {@code limit} is not negative and {@code allMilestones} should already be ordered
     * in the desired priority
     * @param currentMilestones
     * @param limit
     * @param allMilestones
     */
    private List<PickerMilestone> getLimitedFirstMilestones(List<PickerMilestone> currentMilestones,
                                                            int limit, List<PickerMilestone> allMilestones) {
        return allMilestones.stream()
                .filter(milestone -> !currentMilestones.contains(milestone))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Optional<PickerMilestone> getExactMatchMilestone(List<PickerMilestone> milestones, String query) {
        return milestones.stream()
                .filter(milestone -> milestone.getTitle().equals(query))
                .findFirst();
    }

    private boolean isMatchingQuery(PickerMilestone milestone, String query) {
        return Utility.containsIgnoreCase(milestone.getTitle(), query);
    }
}
