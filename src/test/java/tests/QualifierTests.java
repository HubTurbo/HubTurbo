package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.Test;

import backend.interfaces.IModel;
import backend.resource.*;
import filter.Parser;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import filter.expression.QualifierType;

public class QualifierTests {
    List<TurboIssue> issues = createSampleIssues();
    List<TurboMilestone> milestones = createSampleMilestone();
    MultiModel testModel = mock(MultiModel.class);

    public QualifierTests() {
        setMilestonesForSampleIssues();
        mockSampleMultiModel();
    }

    private void setMilestonesForSampleIssues() {
        issues.get(0).setMilestone(milestones.get(0));
        issues.get(1).setMilestone(milestones.get(1));
        issues.get(2).setMilestone(milestones.get(2));
        issues.get(3).setMilestone(milestones.get(3));
    }

    private void mockSampleMultiModel() {
        when(testModel.getIssues()).thenReturn(issues);
        when(testModel.getMilestones()).thenReturn(milestones);

        when(testModel.getMilestoneOfIssue(any(TurboIssue.class)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArguments();
                    TurboIssue issue = (TurboIssue) args[0];

                    if (!issue.getMilestone().isPresent()) {
                        return Optional.empty();
                    }

                    Integer id = issue.getMilestone().get();

                    for (TurboMilestone milestone : milestones) {
                        if (milestone.getId() == id) {
                            return Optional.of(milestone);
                        }
                    }
                    return Optional.empty();
                });
    }

    private List<Qualifier> getMetaQualifiers(FilterExpression filterExpression) {
        return filterExpression.find(Qualifier::isMetaQualifier);
    }

    /**
     * This method can only used to test the 'sort' qualifier with keys
     * comments, repo, updated, date, nonSelfUpdate, assignee, id
     *
     * @param filterExpression
     * @return A Comparator for TurboIssue or
     * null if there is no sort qualifier in the expression
     */
    private Comparator<TurboIssue> getComparatorForSortQualifier(String filterExpression) {
        List<Qualifier> metaQualifiers = getMetaQualifiers(Parser.parse(filterExpression));

        for (Qualifier metaQualifier : metaQualifiers) {
            if (metaQualifier.getType() == QualifierType.SORT) {
                return metaQualifier.getCompoundSortComparator(testModel, false);
            }
        }

        return null;
    }

    private TurboIssue createIssueWithAssignee(
            String repoId, int id, String title, String creator, String assignee,
            LocalDateTime createdAt, boolean isPullRequest) {
        TurboIssue issue = new TurboIssue(repoId, id, title, creator, createdAt, isPullRequest);
        if (!assignee.isEmpty()) {
            issue.setAssignee(assignee);
        }
        return issue;
    }

    private List<TurboIssue> createSampleIssues() {
        TurboIssue issue1 = createIssueWithAssignee(
                "c/c", 1, "Issue1", "c", "java",
                LocalDateTime.of(2015, 1, 1, 1, 1), false);
        TurboIssue issue2 = createIssueWithAssignee(
                "a/a", 2, "Issue2", "a", "ada",
                LocalDateTime.of(2014, 10, 1, 2, 3), true);
        TurboIssue issue3 = createIssueWithAssignee(
                "b/b", 3, "Issue3", "b", "c++",
                LocalDateTime.of(2016, 2, 11, 23, 59), false);
        TurboIssue issue4 = createIssueWithAssignee(
                "e/e", 4, "Issue4", "e", "python",
                LocalDateTime.of(2000, 1, 11, 0, 0), true);
        TurboIssue issue5 = createIssueWithAssignee(
                "d/d", 5, "Issue5", "d", "javascript",
                LocalDateTime.of(2015, 1, 1, 1, 1), false);

        List<TurboIssue> sampleIssues = new ArrayList<TurboIssue>();
        sampleIssues.add(issue1);
        sampleIssues.add(issue2);
        sampleIssues.add(issue3);
        sampleIssues.add(issue4);
        sampleIssues.add(issue5);

        return sampleIssues;
    }

    private List<TurboMilestone> createSampleMilestone() {
        TurboMilestone milestone1 = new TurboMilestone("testrepo/testrepo", 1, "V1");
        milestone1.setDueDate(Optional.of(LocalDate.of(2015, 1, 10)));
        TurboMilestone milestone2 = new TurboMilestone("testrepo/testrepo", 2, "V2");
        milestone2.setDueDate(Optional.of(LocalDate.of(2015, 1, 30)));
        TurboMilestone milestone3 = new TurboMilestone("testrepo/testrepo", 3, "V3");
        milestone3.setDueDate(Optional.of(LocalDate.of(2015, 2, 14)));
        TurboMilestone milestone4 = new TurboMilestone("testrepo/testrepo", 4, "V4");
        TurboMilestone milestone5 = new TurboMilestone("testrepo/testrepo", 5, "V5");
        milestone5.setOpen(false);

        List<TurboMilestone> sampleMilestones = new ArrayList<>();
        sampleMilestones.add(milestone1);
        sampleMilestones.add(milestone2);
        sampleMilestones.add(milestone3);
        sampleMilestones.add(milestone4);
        sampleMilestones.add(milestone5);

        return sampleMilestones;
    }

    /**
     * Tests if getComparatorForSortQualifie returns a valid comparator for "sort:assignee"
     * and null if there is no sort qualifier
     */
    @Test
    public void testSortQualifer1() {
        List<TurboIssue> issues = new ArrayList<>();
        Comparator<TurboIssue> comparator = getComparatorForSortQualifier("sort:assignee");
        Collections.sort(issues, comparator);

        assertTrue(issues.isEmpty());
        assertTrue(getComparatorForSortQualifier("") == null);
    }

    /**
     * Tests sort qualifier with only assignee key
     */
    @Test
    public void testSortQualifer2() {
        List<TurboIssue> issues = testModel.getIssues();
        Comparator<TurboIssue> comparator = getComparatorForSortQualifier("sort:assignee");

        Collections.shuffle(issues);
        Collections.sort(issues, comparator);
        List<Integer> expected = Arrays.asList(2, 3, 1, 5, 4);
        List<Integer> actual = issues.stream().map(TurboIssue::getId).collect(Collectors.toList());
        assertEquals(expected, actual);
    }

    @Test
    public void sortByAssignee_isNonSelfUpdate() {
        List<TurboIssue> issues = testModel.getIssues();
        Comparator<TurboIssue> comparator = getComparatorForSortQualifier("sort:nonSelfUpdate, assignee");

        Collections.shuffle(issues);
        Collections.sort(issues, comparator);
        List<Integer> expected = Arrays.asList(4, 2, 1, 5, 3);
        List<Integer> actual = issues.stream().map(TurboIssue::getId).collect(Collectors.toList());
        assertEquals(expected, actual);

        // test: nonSelfUpdate alias
        comparator = getComparatorForSortQualifier("sort:nonSelfUpdate, assignee");
        Collections.shuffle(issues);
        Collections.sort(issues, comparator);
        actual = issues.stream().map(TurboIssue::getId).collect(Collectors.toList());
        assertEquals(expected, actual);
    }

    @Test
    public void sortByDate() {
        List<TurboIssue> issues = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            TurboIssue issue = new TurboIssue(FilterEvalTests.REPO, i, "");
            issue.setUpdatedAt(LocalDateTime.of(2015, 6, 4 + i, 12, 0));
            issues.add(issue);
        }
        Comparator<TurboIssue> comparator = getComparatorForSortQualifier("sort:date");

        Collections.shuffle(issues);
        Collections.sort(issues, comparator);
        List<Integer> expected = Arrays.asList(0, 1, 2, 3, 4);
        List<Integer> actual = issues.stream().map(TurboIssue::getId).collect(Collectors.toList());
        assertEquals(expected, actual);

        // test: date alias
        comparator = getComparatorForSortQualifier("sort:d");
        Collections.shuffle(issues);
        Collections.sort(issues, comparator);
        actual = issues.stream().map(TurboIssue::getId).collect(Collectors.toList());
        assertEquals(expected, actual);
    }

    /**
     * Tests sort qualifier with only ~assignee key
     */
    @Test
    public void testSortQualifer3() {
        List<TurboIssue> issues = testModel.getIssues();
        Comparator<TurboIssue> comparator = getComparatorForSortQualifier("sort:~assignee");

        Collections.shuffle(issues);
        Collections.sort(issues, comparator);
        List<Integer> expected = Arrays.asList(4, 5, 1, 3, 2);
        List<Integer> actual = issues.stream().map(TurboIssue::getId).collect(Collectors.toList());
        assertEquals(expected, actual);
    }

    /**
     * Tests sort qualifier with issues with empty assignee
     */
    @Test
    public void testSortQualifer4() {
        List<TurboIssue> issues = testModel.getIssues();
        Comparator<TurboIssue> comparator = getComparatorForSortQualifier("sort:assignee,id");
        Comparator<TurboIssue> reverseComparator = getComparatorForSortQualifier("sort:~assignee,id");

        TurboIssue issue6 = createIssueWithAssignee(
                "b/b", 6, "Issue6", "b", "",
                LocalDateTime.of(2013, 1, 2, 3, 4), true);
        TurboIssue issue7 = createIssueWithAssignee(
                "b/b", 7, "Issue7", "b", "",
                LocalDateTime.of(2011, 4, 2, 3, 1), false);
        issues.add(issue6);
        issues.add(issue7);

        Collections.shuffle(issues);
        Collections.sort(issues, comparator);
        List<Integer> expected = Arrays.asList(2, 3, 1, 5, 4, 6, 7);
        List<Integer> actual = issues.stream().map(TurboIssue::getId).collect(Collectors.toList());
        assertEquals(expected, actual);

        Collections.shuffle(issues);
        Collections.sort(issues, reverseComparator);
        expected = Arrays.asList(6, 7, 4, 5, 1, 3, 2);
        actual = issues.stream().map(TurboIssue::getId).collect(Collectors.toList());
        assertEquals(expected, actual);
    }

    /**
     * Tests sort qualifier with keys: repo, id and assignee
     */
    @Test
    public void testSortQualifer5() {
        List<TurboIssue> issues = testModel.getIssues();
        Comparator<TurboIssue> comparator = getComparatorForSortQualifier("sort:repo,id,assignee");

        TurboIssue issue6 = createIssueWithAssignee(
                "d/d", 3, "Issue6", "d", "haskell",
                LocalDateTime.of(2011, 3, 1, 1, 2), true);
        TurboIssue issue7 = createIssueWithAssignee(
                "d/d", 3, "Issue7", "d", "javascript",
                LocalDateTime.of(2011, 3, 1, 1, 2), true);
        issues.add(issue6);
        issues.add(issue7);

        Collections.shuffle(issues);
        Collections.sort(issues, comparator);

        assertEquals("[a/a #2, b/b #3, c/c #1, d/d #3, d/d #3, d/d #5, e/e #4]",
                     issues.toString());
    }

    /**
     * Tests sort qualifier with milestone key. Milestones with due date are sorted from
     * latest to earliest, then comes milestones without due date and no milestone
     */
    @Test
    public void testSortMilestone1() {
        List<TurboIssue> issues = testModel.getIssues();
        TurboIssue issue6 = new TurboIssue("testrepo/testrepo", 6, "Issue6");
        TurboIssue issue7 = new TurboIssue("testrepo/testrepo", 7, "Issue7");
        issue7.setMilestone(testModel.getMilestones().get(3));
        issues.add(issue6);
        issues.add(issue7);

        Comparator<TurboIssue> comparator = getComparatorForSortQualifier("sort:milestone,id");

        Collections.shuffle(issues);
        Collections.sort(issues, comparator);


        List<Integer> expected = Arrays.asList(4, 7, 3, 2, 1, 5, 6);
        List<Integer> actual = issues.stream().map(TurboIssue::getId).collect(Collectors.toList());
        assertEquals(expected, actual);
    }

    /**
     * Tests sort qualifier with inverse milestone key.
     */
    @Test
    public void testSortMilestone2() {
        List<TurboIssue> issues = testModel.getIssues();
        Comparator<TurboIssue> comparator = getComparatorForSortQualifier("sort:~milestone,id");
        TurboIssue issue6 = new TurboIssue("testrepo/testrepo", 6, "Issue6");
        TurboIssue issue7 = new TurboIssue("testrepo/testrepo", 7, "Issue7");
        issue7.setMilestone(testModel.getMilestones().get(3));
        issues.add(issue6);
        issues.add(issue7);

        Collections.shuffle(issues);
        Collections.sort(issues, comparator);

        List<Integer> expected = Arrays.asList(5, 6, 1, 2, 3, 4, 7);
        List<Integer> actual = issues.stream().map(TurboIssue::getId).collect(Collectors.toList());
        assertEquals(expected, actual);
    }

    @Test
    public void milestoneSorting_closedMilestoneWithoutDueDate_prioritizeRecentMilestone() {
        List<TurboIssue> issues = testModel.getIssues();
        Comparator<TurboIssue> comparator = getComparatorForSortQualifier("sort:milestone,id");
        TurboIssue issue6 = new TurboIssue("testrepo/testrepo", 6, "Issue6");
        TurboIssue issue7 = new TurboIssue("testrepo/testrepo", 7, "Issue7");
        issue7.setMilestone(testModel.getMilestones().get(3));
        issue6.setMilestone(testModel.getMilestones().get(4));
        issues.add(issue6);
        issues.add(issue7);

        Collections.shuffle(issues);
        Collections.sort(issues, comparator);

        // milestone 6 and 5 should be at the end of the sorted list since closed milestone without
        // due date is considered to have a due date very far in the past
        List<Integer> expected = Arrays.asList(4, 7, 3, 2, 1, 6, 5);
        List<Integer> actual = issues.stream().map(TurboIssue::getId).collect(Collectors.toList());
        assertEquals(expected, actual);
    }

    @Test
    public void sort() {
        FilterEvalTests filterEvalTester = new FilterEvalTests();
        TurboIssue issue = new TurboIssue(FilterEvalTests.REPO, 1, "");

        // Being a meta-qualifier, this doesn't have any effect
        assertEquals(true, filterEvalTester.matches("sort:id", issue));
        assertEquals(true, filterEvalTester.matches("sort:id, ~repo", issue));
        assertEquals(true, filterEvalTester.matches("sort:~id, NOT repo", issue));
    }

    @Test
    public void repoOrdering() {
        List<TurboIssue> issues = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            issues.add(new TurboIssue(FilterEvalTests.REPO, i, ""));
        }
        for (int i = 5; i < 10; i++) {
            issues.add(new TurboIssue("aaa/aaa", i, ""));
        }

        IModel model = TestUtils.singletonModel(
                new Model(FilterEvalTests.REPO, issues,
                          new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

        List<TurboIssue> renderedIssues = new ArrayList<>(issues);


        // test: sorting by repo
        assertSorted(renderedIssues, Arrays.asList(5, 6, 7, 8, 9, 0, 1, 2, 3, 4),
                     model, "repo", false, false);

        // repo alias
        assertSorted(renderedIssues, Arrays.asList(5, 6, 7, 8, 9, 0, 1, 2, 3, 4),
                     model, "r", false, false);

        // inverted sort order
        assertSorted(renderedIssues, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
                     model, "repo", true, false);
    }

    @Test
    public void updatedOrdering() {
        List<TurboIssue> issues = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            TurboIssue issue = new TurboIssue(FilterEvalTests.REPO, i, "");
            issue.setUpdatedAt(LocalDateTime.of(2015, 6, 4 + i, 12, 0));
            issues.add(issue);
        }

        IModel model = TestUtils.singletonModel(
                new Model(FilterEvalTests.REPO, issues,
                          new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

        List<TurboIssue> renderedIssues = new ArrayList<>(issues);

        // test: sorting by updated 
        assertSorted(renderedIssues, Arrays.asList(0, 1, 2, 3, 4),
                     model, "updated", false, false);
        // updated alias
        assertSorted(renderedIssues, Arrays.asList(0, 1, 2, 3, 4),
                     model, "u", false, false);

        // inverted sort order
        assertSorted(renderedIssues, Arrays.asList(4, 3, 2, 1, 0),
                     model, "updated", true, false);
    }

    @Test
    public void idOrdering() {
        List<TurboIssue> issues = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            issues.add(new TurboIssue(FilterEvalTests.REPO, i, ""));
        }

        IModel model = TestUtils.singletonModel(
                new Model(FilterEvalTests.REPO, issues,
                          new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

        List<TurboIssue> renderedIssues = new ArrayList<>(issues);


        // test: sorting by id
        assertSorted(renderedIssues, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7),
                     model, "id", false, false);

        // inverted sort order
        assertSorted(renderedIssues, Arrays.asList(7, 6, 5, 4, 3, 2, 1, 0),
                     model, "id", true, false);

    }

    @Test
    public void commentsOrdering() {
        List<TurboIssue> issues = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            TurboIssue issue = new TurboIssue(FilterEvalTests.REPO, i, "");
            issue.setCommentCount(i);
            issues.add(issue);
        }

        IModel model = TestUtils.singletonModel(
                new Model(FilterEvalTests.REPO, issues,
                          new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

        List<TurboIssue> renderedIssues = new ArrayList<>(issues);

        // test: sorting by comments
        assertSorted(renderedIssues, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7),
                     model, "comments", false, false);

        // comments alias
        assertSorted(renderedIssues, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7),
                     model, "cm", false, false);

        // inverted sort order
        assertSorted(renderedIssues, Arrays.asList(7, 6, 5, 4, 3, 2, 1, 0),
                     model, "comments", true, false);

    }

    @Test
    public void labelGroupOrdering() {

        // Labels and issues

        TurboLabel one = new TurboLabel(FilterEvalTests.REPO, "test.1");
        TurboLabel two = new TurboLabel(FilterEvalTests.REPO, "test.2");
        TurboLabel a = new TurboLabel(FilterEvalTests.REPO, "test.a");
        TurboLabel other = new TurboLabel(FilterEvalTests.REPO, "something");

        List<TurboLabel> labels = new ArrayList<>();
        labels.add(one);
        labels.add(two);
        labels.add(a);
        labels.add(other);

        List<TurboIssue> issues = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            issues.add(new TurboIssue(FilterEvalTests.REPO, i, ""));
        }

        issues.get(0).getLabels().addAll(Arrays.asList("test.1"));
        issues.get(1).getLabels().addAll(Arrays.asList("test.2"));
        issues.get(2).getLabels().addAll(Arrays.asList("test.a"));
        issues.get(3).getLabels().addAll(Arrays.asList("test.1", "test.2"));
        issues.get(4).getLabels().addAll(Arrays.asList("test.a", "test.2"));
        issues.get(5).getLabels().addAll(Arrays.asList("test.1", "test.2", "test.a"));
        issues.get(6).getLabels().addAll(Arrays.asList("something"));
        // issues.get(7) has no labels

        for (int i = 0; i < 8; i++) {
            issues.get(i).setTitle(issues.get(i).getLabels().toString());
        }

        // Construct model
        IModel model = TestUtils.singletonModel(
                new Model(FilterEvalTests.REPO, issues, labels, new ArrayList<>(), new ArrayList<>()));

        List<TurboIssue> renderedIssues = new ArrayList<>(issues);

        // lexicographical within groups, with those outside the group arranged last, by size
        // (being last can mean either larger or smaller depending on inversion)

        assertSorted(renderedIssues, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7),
                     model, "test", false, false);

        assertSorted(renderedIssues, Arrays.asList(5, 4, 3, 2, 1, 0, 6, 7),
                     model, "test", true, false);
    }

    @Test
    public void statusOrdering() {
        List<TurboIssue> issues = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            TurboIssue issue = new TurboIssue(FilterEvalTests.REPO, i, "");
            if (i < 4) {
                issue.setOpen(true);
            } else {
                issue.setOpen(false);
            }
            issues.add(issue);
        }
        IModel model = TestUtils.singletonModel(
                new Model(FilterEvalTests.REPO, issues,
                          new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

        List<TurboIssue> renderedIssues = new ArrayList<>(issues);

        // test: sorting by status 
        assertSorted(renderedIssues, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7),
                     model, "status", false, false);

        // status alias
        assertSorted(renderedIssues, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7),
                     model, "st", false, false);

        // inverted sort order
        assertSorted(renderedIssues, Arrays.asList(4, 5, 6, 7, 0, 1, 2, 3),
                     model, "status", true, false);

    }

    /**
     * Ensures that TurboIssues are ordered in a particular way given some sorting criteria
     */
    private void assertSorted(List<TurboIssue> issues, List<Integer> expectedIds,
                              IModel model, String sortCriteria,
                              boolean isInverted, boolean isNonSelfUpdate) {
        Collections.sort(issues,
                         Qualifier.getSortComparator(model, sortCriteria, isInverted, isNonSelfUpdate));

        assertEquals(expectedIds, getIds(issues));
    }

    /**
     * Get list of ids from TurboIssues
     */
    private List<Integer> getIds(List<TurboIssue> issues) {
        return issues.stream()
                .map(TurboIssue::getId)
                .collect(Collectors.toList());
    }
}
