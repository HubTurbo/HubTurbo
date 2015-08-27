package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import backend.interfaces.IModel;
import backend.resource.Model;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import filter.Parser;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;

public class SortQualifierTests {

    private List<Qualifier> getMetaQualifiers(FilterExpression filterExpression) {
        return filterExpression.find(Qualifier::isMetaQualifier);
    }

    /**
     * This method can only used to test the 'sort' qualifier with keys
     * comments, repo, updated, date, nonSelfUpdate, assignee, id
     * @param filterExpression
     * @return A Comparator for TurboIssue or
     *         null if there is no sort qualifier in the expression
     */
    private Comparator<TurboIssue> getComparatorForSortQualifier(String filterExpression) {
        List<Qualifier> metaQualifiers = getMetaQualifiers(Parser.parse(filterExpression));

        for (Qualifier metaQualifier : metaQualifiers) {
            if (metaQualifier.getName().equals("sort")) {
                return metaQualifier.getCompoundSortComparator(null, false);
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

    private List<TurboIssue> getSampleIssues() {
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
        List<TurboIssue> issues = getSampleIssues();
        Comparator<TurboIssue> comparator = getComparatorForSortQualifier("sort:assignee");

        Collections.shuffle(issues);
        Collections.sort(issues, comparator);
        assertEquals("[#2 Issue2, #3 Issue3, #1 Issue1, #5 Issue5, #4 Issue4]", issues.toString());
    }

    /**
     * Tests sort qualifier with only ~assignee key
     */
    @Test
    public void testSortQualifer3() {
        List<TurboIssue> issues = getSampleIssues();
        Comparator<TurboIssue> comparator = getComparatorForSortQualifier("sort:~assignee");

        Collections.shuffle(issues);
        Collections.sort(issues, comparator);
        assertEquals("[#4 Issue4, #5 Issue5, #1 Issue1, #3 Issue3, #2 Issue2]", issues.toString());
    }

    /**
     * Tests sort qualifier with issues with empty assignee
     */
    @Test
    public void testSortQualifer4() {
        List<TurboIssue> issues = getSampleIssues();
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
        String expected = "[#2 Issue2, #3 Issue3, #1 Issue1, #5 Issue5, #4 Issue4, " +
                          "#6 Issue6, #7 Issue7]";
        assertEquals(expected, issues.toString());

        Collections.shuffle(issues);
        Collections.sort(issues, reverseComparator);
        expected = "[#6 Issue6, #7 Issue7, " +
                   "#4 Issue4, #5 Issue5, #1 Issue1, #3 Issue3, #2 Issue2]";
        assertEquals(expected, issues.toString());
    }

    /**
     * Tests sort qualifier with keys: repo, id and assignee
     */
    @Test
    public void testSortQualifer5() {
        List<TurboIssue> issues = getSampleIssues();
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
        assertEquals("[#2 Issue2, #3 Issue3, #1 Issue1, #3 Issue6, #3 Issue7, #5 Issue5, #4 Issue4]",
                     issues.toString());
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

        Collections.sort(renderedIssues,
            Qualifier.getSortComparator(model, "repo", false, false));

        assertEquals(Arrays.asList(5, 6, 7, 8, 9, 0, 1, 2, 3, 4), renderedIssues.stream()
            .map(TurboIssue::getId)
            .collect(Collectors.toList()));

        Collections.sort(renderedIssues,
            Qualifier.getSortComparator(model, "repo", true, false));

        assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), renderedIssues.stream()
            .map(TurboIssue::getId)
            .collect(Collectors.toList()));
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

        Collections.sort(renderedIssues,
            Qualifier.getSortComparator(model, "updated", false, false));

        assertEquals(Arrays.asList(0, 1, 2, 3, 4), renderedIssues.stream()
            .map(TurboIssue::getId)
            .collect(Collectors.toList()));

        Collections.sort(renderedIssues,
            Qualifier.getSortComparator(model, "updated", true, false));

        assertEquals(Arrays.asList(4, 3, 2, 1, 0), renderedIssues.stream()
            .map(TurboIssue::getId)
            .collect(Collectors.toList()));
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

        Collections.sort(renderedIssues,
            Qualifier.getSortComparator(model, "id", false, false));

        assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7), renderedIssues.stream()
            .map(TurboIssue::getId)
            .collect(Collectors.toList()));

        Collections.sort(renderedIssues,
            Qualifier.getSortComparator(model, "id", true, false));

        assertEquals(Arrays.asList(7, 6, 5, 4, 3, 2, 1, 0), renderedIssues.stream()
            .map(TurboIssue::getId)
            .collect(Collectors.toList()));
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

        Collections.sort(renderedIssues,
            Qualifier.getSortComparator(model, "comments", false, false));

        assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7), renderedIssues.stream()
            .map(TurboIssue::getId)
            .collect(Collectors.toList()));

        Collections.sort(renderedIssues,
            Qualifier.getSortComparator(model, "comments", true, false));

        assertEquals(Arrays.asList(7, 6, 5, 4, 3, 2, 1, 0), renderedIssues.stream()
            .map(TurboIssue::getId)
            .collect(Collectors.toList()));
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

        Collections.sort(renderedIssues,
            Qualifier.getLabelGroupComparator(model, "test", false));

        assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7), renderedIssues.stream()
            .map(TurboIssue::getId)
            .collect(Collectors.toList()));

        Collections.sort(renderedIssues,
            Qualifier.getLabelGroupComparator(model, "test", true));

        assertEquals(Arrays.asList(5, 4, 3, 2, 1, 0, 6, 7), renderedIssues.stream()
            .map(TurboIssue::getId)
            .collect(Collectors.toList()));
    }
}
