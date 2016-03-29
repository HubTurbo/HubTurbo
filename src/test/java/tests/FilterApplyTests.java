package tests;

import backend.interfaces.IModel;
import backend.resource.*;
import filter.QualifierApplicationException;
import filter.expression.*;

import org.junit.Test;
import prefs.Preferences;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import static filter.expression.QualifierType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class FilterApplyTests {

    private static final String REPO = "test/test";

    private final IModel empty;

    public FilterApplyTests() {
        empty = new MultiModel(mock(Preferences.class));
        empty.setDefaultRepo(REPO);
    }

    @Test
    public void label() {
        TurboIssue issue = new TurboIssue(REPO, 1, "");
        TurboLabel label = new TurboLabel(REPO, "aabb");
        assertEquals(true, issue.getLabels().isEmpty());

        // Simplest successful case
        try {
            new Qualifier(LABEL, "aabb").applyTo(issue, TestUtils.modelWith(issue, label));
            assertEquals(false, issue.getLabels().isEmpty());
            assertEquals("aabb", issue.getLabels().get(0));
        } catch (QualifierApplicationException e) {
            fail();
        }

        // Partial match
        issue = new TurboIssue(REPO, 1, "");
        TurboLabel label2 = new TurboLabel(REPO, "bbcc");
        IModel model = TestUtils.singletonModel(new Model(REPO,
                                                          new ArrayList<>(Arrays.asList(issue)),
                                                          new ArrayList<>(Arrays.asList(label, label2)),
                                                          new ArrayList<>(),
                                                          new ArrayList<>()));

        try {
            new Qualifier(LABEL, "aa").applyTo(issue, model);
            assertEquals(false, issue.getLabels().isEmpty());
            assertEquals("aabb", issue.getLabels().get(0));
        } catch (QualifierApplicationException e) {
            fail();
        }

        // Ambigous label
        issue = new TurboIssue(REPO, 1, "");
        label2 = new TurboLabel(REPO, "bbcc");
        model = TestUtils.singletonModel(new Model(REPO,
                                                   new ArrayList<>(Arrays.asList(issue)),
                                                   new ArrayList<>(Arrays.asList(label, label2)),
                                                   new ArrayList<>(),
                                                   new ArrayList<>()));

        try {
            new Qualifier(LABEL, "bb").applyTo(issue, model);
            fail();
        } catch (QualifierApplicationException ignored) {
        }

        // Non-existent label
        try {
            new Qualifier(LABEL, "dd").applyTo(issue, TestUtils.modelWith(issue, label));
            fail();
        } catch (QualifierApplicationException ignored) {
        }

        // Non-string
        try {
            new Qualifier(LABEL, 1).applyTo(issue, TestUtils.modelWith(issue, label));
            fail();
        } catch (QualifierApplicationException ignored) {
        }
    }

    @Test
    public void milestone() {
        TurboIssue issue = new TurboIssue(REPO, 1, "");
        TurboMilestone milestone = new TurboMilestone(REPO, 7, "v1");
        assertEquals(false, issue.getMilestone().isPresent());

        // Simplest successful case
        try {
            new Qualifier(MILESTONE, "v1").applyTo(issue, TestUtils.modelWith(issue, milestone));
            assertEquals(true, issue.getMilestone().isPresent());
            assertEquals(Integer.valueOf(7), issue.getMilestone().get());
        } catch (QualifierApplicationException e) {
            fail();
        }

        // Partial match
        issue = new TurboIssue(REPO, 1, "");
        TurboMilestone milestone2 = new TurboMilestone(REPO, 9, "v2");
        IModel model = TestUtils.singletonModel(new Model(REPO,
                                                          new ArrayList<>(Arrays.asList(issue)),
                                                          new ArrayList<>(),
                                                          new ArrayList<>(Arrays.asList(milestone, milestone2)),
                                                          new ArrayList<>()));

        try {
            new Qualifier(MILESTONE, "1").applyTo(issue, model);
            assertEquals(true, issue.getMilestone().isPresent());
            assertEquals(Integer.valueOf(7), issue.getMilestone().get());
        } catch (QualifierApplicationException e) {
            fail();
        }

        // Ambigous milestone
        issue = new TurboIssue(REPO, 1, "");
        milestone2 = new TurboMilestone(REPO, 9, "v2");
        model = TestUtils.singletonModel(new Model(REPO,
                                                   new ArrayList<>(Arrays.asList(issue)),
                                                   new ArrayList<>(),
                                                   new ArrayList<>(Arrays.asList(milestone, milestone2)),
                                                   new ArrayList<>()));

        try {
            new Qualifier(MILESTONE, "v").applyTo(issue, model);
            fail();
        } catch (QualifierApplicationException ignored) {
        }

        // Non-existent milestone
        try {
            new Qualifier(MILESTONE, "3").applyTo(issue, TestUtils.modelWith(issue, milestone));
            fail();
        } catch (QualifierApplicationException ignored) {
        }

        // Non-string
        try {
            new Qualifier(MILESTONE, 1).applyTo(issue, TestUtils.modelWith(issue, milestone));
            fail();
        } catch (QualifierApplicationException ignored) {
        }
    }

    @Test
    public void assignee() {
        TurboIssue issue = new TurboIssue(REPO, 1, "");
        TurboUser assignee = new TurboUser(REPO, "aabb");
        assertEquals(false, issue.getAssignee().isPresent());

        // Simplest successful case
        try {
            new Qualifier(ASSIGNEE, "aabb").applyTo(issue, TestUtils.modelWith(issue, assignee));
            assertEquals(true, issue.getAssignee().isPresent());
            assertEquals("aabb", issue.getAssignee().get());
        } catch (QualifierApplicationException e) {
            fail();
        }

        // Partial match
        issue = new TurboIssue(REPO, 1, "");
        TurboUser assignee2 = new TurboUser(REPO, "bbcc");
        IModel model = TestUtils.singletonModel(new Model(REPO,
                                                          new ArrayList<>(Arrays.asList(issue)),
                                                          new ArrayList<>(),
                                                          new ArrayList<>(),
                                                          new ArrayList<>(Arrays.asList(assignee, assignee2))));

        try {
            new Qualifier(ASSIGNEE, "aa").applyTo(issue, model);
            assertEquals(true, issue.getAssignee().isPresent());
            assertEquals("aabb", issue.getAssignee().get());
        } catch (QualifierApplicationException e) {
            fail();
        }

        // Ambigous assignee
        issue = new TurboIssue(REPO, 1, "");
        assignee2 = new TurboUser(REPO, "bbcc");
        model = TestUtils.singletonModel(new Model(REPO,
                                                   new ArrayList<>(Arrays.asList(issue)),
                                                   new ArrayList<>(),
                                                   new ArrayList<>(),
                                                   new ArrayList<>(Arrays.asList(assignee, assignee2))));

        try {
            new Qualifier(ASSIGNEE, "bb").applyTo(issue, model);
            fail();
        } catch (QualifierApplicationException ignored) {
        }

        // Non-existent assignee
        try {
            new Qualifier(ASSIGNEE, "dd").applyTo(issue, TestUtils.modelWith(issue, assignee));
            fail();
        } catch (QualifierApplicationException ignored) {
        }

        // Non-string
        try {
            new Qualifier(ASSIGNEE, 1).applyTo(issue, TestUtils.modelWith(issue, assignee));
            fail();
        } catch (QualifierApplicationException ignored) {
        }
    }

    @Test
    public void state() {
        TurboIssue issue = new TurboIssue(REPO, 1, "");
        assertEquals(true, issue.isOpen());
        try {
            new Qualifier(STATE, "closed").applyTo(issue, empty);
            assertEquals(false, issue.isOpen());
            new Qualifier(STATE, "open").applyTo(issue, empty);
            assertEquals(true, issue.isOpen());
        } catch (QualifierApplicationException e) {
            fail();
        }

        issue.setOpen(true);
        assertEquals(true, issue.isOpen());
        try {
            new Qualifier(STATE, "something").applyTo(issue, empty);
            fail();
        } catch (QualifierApplicationException ignored) {
        }
    }

    @Test
    public void compoundApplication() {
        TurboIssue issue = new TurboIssue(REPO, 1, "");
        TurboLabel label = new TurboLabel(REPO, "bug");
        TurboMilestone milestone = new TurboMilestone(REPO, 7, "v1");

        assertEquals(0, issue.getLabels().size());
        assertEquals(false, issue.getMilestone().isPresent());
        assertEquals(true, issue.isOpen());

        try {
            Qualifier a = new Qualifier(LABEL, "bug");
            Qualifier b = new Qualifier(MILESTONE, "v1");
            Qualifier c = new Qualifier(STATE, "closed");
            Conjunction all = new Conjunction(a, new Conjunction(b, c));

            all.applyTo(issue, TestUtils.modelWith(issue, label, milestone));

            assertEquals(1, issue.getLabels().size());
            assertEquals("bug", issue.getLabels().get(0));

            assertEquals(true, issue.getMilestone().isPresent());
            assertEquals(Integer.valueOf(7), issue.getMilestone().get());

            assertEquals(false, issue.isOpen());

        } catch (QualifierApplicationException e) {
            fail();
        }
    }

    @Test
    public void invalidStructure() {

        // Duplicate names
        assertEquals(false, new Conjunction(
                Qualifier.EMPTY,
                Qualifier.EMPTY).canBeAppliedToIssue());

        assertEquals(true, Qualifier.EMPTY.canBeAppliedToIssue());
        assertEquals(true, new Conjunction(
                new Qualifier(ID, ""),
                Qualifier.EMPTY).canBeAppliedToIssue());
        assertEquals(true, new Conjunction(
                new Qualifier(ID, ""),
                new Conjunction(
                        new Qualifier(SORT, ""),
                        Qualifier.EMPTY)).canBeAppliedToIssue());

        // It doesn't matter what the cntents of the disjunction are
        assertEquals(false, new Disjunction(null, null).canBeAppliedToIssue());
        assertEquals(false, new Disjunction(Qualifier.EMPTY, Qualifier.EMPTY).canBeAppliedToIssue());
        assertEquals(false, new Disjunction(
                Qualifier.EMPTY,
                new Conjunction(Qualifier.EMPTY, Qualifier.EMPTY)).canBeAppliedToIssue());
        assertEquals(false, new Conjunction(
                Qualifier.EMPTY,
                new Disjunction(Qualifier.EMPTY, Qualifier.EMPTY)).canBeAppliedToIssue());

        // Negation could potentially work, but in the current implementation the contents don't matter
        assertEquals(false, new Negation(null).canBeAppliedToIssue());
        assertEquals(false, new Negation(Qualifier.EMPTY).canBeAppliedToIssue());
    }

    @Test
    public void invalidQualifiers() {
        TurboIssue issue = new TurboIssue(REPO, 1, "");
        try {
            new Qualifier(TITLE, "").applyTo(issue, empty);
            fail("title cannot be applied to issue");
        } catch (QualifierApplicationException ignored) {
        }
        try {
            new Qualifier(DESCRIPTION, "").applyTo(issue, empty);
            fail("desc cannot be applied to issue");
        } catch (QualifierApplicationException ignored) {
        }
        try {
            new Qualifier(KEYWORD, "").applyTo(issue, empty);
            fail("keyword cannot be applied to issue");
        } catch (QualifierApplicationException ignored) {
        }
        try {
            new Qualifier(ID, 1).applyTo(issue, empty);
            fail("id cannot be applied to issue");
        } catch (QualifierApplicationException ignored) {
        }
        try {
            new Qualifier(CREATED, LocalDate.now()).applyTo(issue, empty);
            fail("created cannot be applied to issue");
        } catch (QualifierApplicationException ignored) {
        }
        try {
            new Qualifier(HAS, "").applyTo(issue, empty);
            fail("has cannot be applied to issue");
        } catch (QualifierApplicationException ignored) {
        }
        try {
            new Qualifier(NO, "").applyTo(issue, empty);
            fail("no cannot be applied to issue");
        } catch (QualifierApplicationException ignored) {
        }
        try {
            // Could potentially work, but this is the current behaviour
            new Qualifier(IS, "").applyTo(issue, empty);
            fail("is cannot be applied to issue");
        } catch (QualifierApplicationException ignored) {
        }
        try {
            new Qualifier(AUTHOR, "").applyTo(issue, empty);
            fail("author cannot be applied to issue");
        } catch (QualifierApplicationException ignored) {
        }
        try {
            new Qualifier(INVOLVES, "").applyTo(issue, empty);
            fail("involves cannot be applied to issue");
        } catch (QualifierApplicationException ignored) {
        }
    }
}
