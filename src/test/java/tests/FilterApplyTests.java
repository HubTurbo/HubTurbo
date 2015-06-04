package tests;

import backend.interfaces.IModel;
import backend.resource.*;
import filter.QualifierApplicationException;
import filter.expression.Conjunction;
import filter.expression.Disjunction;
import filter.expression.Negation;
import filter.expression.Qualifier;
import org.junit.Test;
import prefs.Preferences;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FilterApplyTests {

	private static final String REPO = "test/test";

	private final IModel empty;

	public FilterApplyTests() {
		empty = new MultiModel(new Preferences(true));
		empty.setDefaultRepo(REPO);
	}

	@Test
	public void label() {
		TurboIssue issue = new TurboIssue(REPO, 1, "");
		TurboLabel label = new TurboLabel(REPO, "bug");
		assertEquals(0, issue.getLabels().size());
		try {
			new Qualifier("label", "bug").applyTo(issue, TestUtils.modelWith(issue, label));
			assertEquals(1, issue.getLabels().size());
			assertEquals("bug", issue.getLabels().get(0));
		} catch (QualifierApplicationException e) {
			fail();
		}
    }

	@Test
	public void milestone() {
		TurboIssue issue = new TurboIssue(REPO, 1, "");
		TurboMilestone milestone = new TurboMilestone(REPO, 7, "v1");
		assertEquals(false, issue.getMilestone().isPresent());
		try {
			new Qualifier("milestone", "v1").applyTo(issue, TestUtils.modelWith(issue, milestone));
			assertEquals(true, issue.getMilestone().isPresent());
			assertEquals(new Integer(7), issue.getMilestone().get());
		} catch (QualifierApplicationException e) {
			fail();
		}
    }

	@Test
	public void assignee() {
		TurboIssue issue = new TurboIssue(REPO, 1, "");
		TurboUser user = new TurboUser(REPO, "aaa");
		assertEquals(false, issue.getAssignee().isPresent());
		try {
			new Qualifier("assignee", "aaa").applyTo(issue, TestUtils.modelWith(issue, user));
			assertEquals(true, issue.getAssignee().isPresent());
			assertEquals("aaa", issue.getAssignee().get());
		} catch (QualifierApplicationException e) {
			fail();
		}
    }

	@Test
	public void state() {
		TurboIssue issue = new TurboIssue(REPO, 1, "");
		assertEquals(true, issue.isOpen());
		try {
			new Qualifier("state", "closed").applyTo(issue, empty);
            assertEquals(false, issue.isOpen());
			new Qualifier("state", "open").applyTo(issue, empty);
            assertEquals(true, issue.isOpen());
		} catch (QualifierApplicationException e) {
			fail();
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
            Qualifier a = new Qualifier("label", "bug");
            Qualifier b = new Qualifier("milestone", "v1");
            Qualifier c = new Qualifier("state", "closed");
            Conjunction all = new Conjunction(a, new Conjunction(b, c));

            all.applyTo(issue, TestUtils.modelWith(issue, label, milestone));

			assertEquals(1, issue.getLabels().size());
			assertEquals("bug", issue.getLabels().get(0));

			assertEquals(true, issue.getMilestone().isPresent());
			assertEquals(new Integer(7), issue.getMilestone().get());

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
	        new Qualifier("a", ""),
	        Qualifier.EMPTY).canBeAppliedToIssue());
        assertEquals(true, new Conjunction(
            new Qualifier("a", ""),
            new Conjunction(
	            new Qualifier("b", ""),
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
			new Qualifier("title", "").applyTo(issue, empty);
			fail("title cannot be applied to issue");
		} catch (QualifierApplicationException ignored) {}
		try {
			new Qualifier("desc", "").applyTo(issue, empty);
			fail("desc cannot be applied to issue");
		} catch (QualifierApplicationException ignored) {}
		try {
			new Qualifier("description", "").applyTo(issue, empty);
			fail("description cannot be applied to issue");
		} catch (QualifierApplicationException ignored) {}
		try {
			new Qualifier("body", "").applyTo(issue, empty);
			fail("body cannot be applied to issue");
		} catch (QualifierApplicationException ignored) {}
		try {
			new Qualifier("keyword", "").applyTo(issue, empty);
			fail("keyword cannot be applied to issue");
		} catch (QualifierApplicationException ignored) {}
		try {
			new Qualifier("id", 1).applyTo(issue, empty);
			fail("id cannot be applied to issue");
		} catch (QualifierApplicationException ignored) {}
		try {
			new Qualifier("created", LocalDate.now()).applyTo(issue, empty);
			fail("created cannot be applied to issue");
		} catch (QualifierApplicationException ignored) {}
		try {
			new Qualifier("has", "").applyTo(issue, empty);
			fail("has cannot be applied to issue");
		} catch (QualifierApplicationException ignored) {}
		try {
			new Qualifier("no", "").applyTo(issue, empty);
			fail("no cannot be applied to issue");
		} catch (QualifierApplicationException ignored) {}
		try {
            // Could potentially work, but this is the current behaviour
			new Qualifier("is", "").applyTo(issue, empty);
			fail("is cannot be applied to issue");
		} catch (QualifierApplicationException ignored) {}
		try {
			new Qualifier("author", "").applyTo(issue, empty);
			fail("author cannot be applied to issue");
		} catch (QualifierApplicationException ignored) {}
		try {
			new Qualifier("creator", "").applyTo(issue, empty);
			fail("creator cannot be applied to issue");
		} catch (QualifierApplicationException ignored) {}
		try {
			new Qualifier("involves", "").applyTo(issue, empty);
			fail("involves cannot be applied to issue");
		} catch (QualifierApplicationException ignored) {}
		try {
			new Qualifier("user", "").applyTo(issue, empty);
			fail("user cannot be applied to issue");
		} catch (QualifierApplicationException ignored) {}
	}
}
