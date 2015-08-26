package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import backend.resource.TurboIssue;
import filter.Parser;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;

public class QualifierTests {
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

        List<TurboIssue> sampleIssues =new ArrayList<TurboIssue>();
        sampleIssues.add(issue1);
        sampleIssues.add(issue2);
        sampleIssues.add(issue3);
        sampleIssues.add(issue4);
        sampleIssues.add(issue5);

        return sampleIssues;
    }

    /**
     * Tests if sort qualifier returns a valid comparator for "sort:assignee"
     */
    @Test
    public void testSortQualifer1() {
        List<TurboIssue> issues = new ArrayList<>();
        Comparator<TurboIssue> comparator = getComparatorForSortQualifier("sort:assignee");
        Collections.sort(issues, comparator);
        assertTrue(issues.isEmpty());
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
     * Tests sort qualifier with keys: repo, id and assignee
     */
    @Test
    public void testSortQualifer4() {
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
}
