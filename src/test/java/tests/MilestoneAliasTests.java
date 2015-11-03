package tests;

import backend.resource.Model;
import backend.resource.TurboMilestone;
import filter.Parser;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import org.junit.Test;
import static org.junit.Assert.*;

import backend.interfaces.IModel;

import java.time.LocalDate;
import java.util.*;

public class MilestoneAliasTests {
    private IModel model;
    public static final String REPO = "test/test";

    @Test
    public void replaceMilestoneAliasesTest() {
        // test: overdue open milestone with no open issues would not be current milestone
        TurboMilestone msCurrMin3 = new TurboMilestone(REPO, 10, "V0.1");
        msCurrMin3.setOpen(true);
        msCurrMin3.setOpenIssues(0);
        msCurrMin3.setDueDate(Optional.of(LocalDate.now().minusMonths(2)));

        // test: sort by due date is correct
        TurboMilestone msCurrMin2 = new TurboMilestone(REPO, 9, "V0.2");
        msCurrMin2.setOpen(false);
        msCurrMin2.setDueDate(Optional.of(LocalDate.now().minusMonths(1)));

        // test: future closed milestone will not be current milestone
        TurboMilestone msCurrMin1 = new TurboMilestone(REPO, 8, "V0.3");
        msCurrMin1.setOpen(false);
        msCurrMin1.setDueDate(Optional.of(LocalDate.now().plusDays(1)));

        // test: earliest future open milestone with 0 open issues will
        // be current milestone
        TurboMilestone msCurr = new TurboMilestone(REPO, 7, "V0.4");
        msCurr.setOpen(true);
        msCurr.setOpenIssues(0);
        msCurr.setDueDate(Optional.of(LocalDate.now().plusMonths(1)));

        // test: sort by due date is correct, even if in the future but
        // closed
        TurboMilestone msCurrPlus1 = new TurboMilestone(REPO, 6, "V0.5");
        msCurrPlus1.setOpen(false);
        msCurrPlus1.setDueDate(Optional.of(LocalDate.now().plusMonths(2)));

        // test: sort by due date is correct
        TurboMilestone msCurrPlus2 = new TurboMilestone(REPO, 5, "V0.6");
        msCurrPlus2.setOpen(true);
        msCurrPlus2.setDueDate(Optional.of(LocalDate.now().plusMonths(3)));

        // test: milestone with no due date should not be included
        TurboMilestone msCurrPlus3 = new TurboMilestone(REPO, 4, "V0.7");
        msCurrPlus3.setDueDate(Optional.empty());

        model = TestUtils.singletonModel(new Model(REPO,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(Arrays.asList(msCurrMin3, msCurrMin2, msCurrMin1,
                        msCurr, msCurrPlus1, msCurrPlus2, msCurrPlus3)),
                new ArrayList<>()));

        FilterExpression noMilestoneAlias;
        List<Qualifier> milestoneQualifiers;

        // test correct conversion
        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:current-3"));
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        milestoneQualifiers.stream().forEach(msQ -> {
            assertEquals(false, msQ.getContent().get().equalsIgnoreCase("current-3"));
            assertEquals(true, msQ.getContent().get().equalsIgnoreCase("V0.1"));
        });
        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr-2"));
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        milestoneQualifiers.stream().forEach(msQ -> {
            assertEquals(false, msQ.getContent().get().equalsIgnoreCase("curr-2"));
            assertEquals(true, msQ.getContent().get().equalsIgnoreCase("V0.2"));
        });
        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr-1"));
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        milestoneQualifiers.stream().forEach(msQ -> {
            assertEquals(false, msQ.getContent().get().equalsIgnoreCase("curr-1"));
            assertEquals(true, msQ.getContent().get().equalsIgnoreCase("V0.3"));
        });
        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr"));
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        milestoneQualifiers.stream().forEach(msQ -> {
            assertEquals(false, msQ.getContent().get().equalsIgnoreCase("curr"));
            assertEquals(true, msQ.getContent().get().equalsIgnoreCase("V0.4"));
        });
        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:current+1"));
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        milestoneQualifiers.stream().forEach(msQ -> {
            assertEquals(false, msQ.getContent().get().equalsIgnoreCase("current+1"));
            assertEquals(true, msQ.getContent().get().equalsIgnoreCase("V0.5"));
        });
        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr+2"));
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        milestoneQualifiers.stream().forEach(msQ -> {
            assertEquals(false, msQ.getContent().get().equalsIgnoreCase("curr+2"));
            assertEquals(true, msQ.getContent().get().equalsIgnoreCase("V0.6"));
        });

        // test milestone no due date not included
        assertMilestoneAliasFalseQualifierSize1("milestone:curr+3");

        // test alias out of bound will be converted to Qualifier.FALSE
        assertMilestoneAliasFalseQualifierSize1("milestone:curr-4");
        assertMilestoneAliasFalseQualifierSize1("milestone:current+4");

        // test wrong alias will not be converted
        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr-"));
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        milestoneQualifiers.stream().forEach(msQ -> {
            assertEquals(true, msQ.getContent().get().equalsIgnoreCase("curr-"));
        });
        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:current-"));
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        milestoneQualifiers.stream().forEach(msQ -> {
            assertEquals(true, msQ.getContent().get().equalsIgnoreCase("current-"));
        });
        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr-asdf"));
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        milestoneQualifiers.stream().forEach(msQ -> {
            assertEquals(true, msQ.getContent().get().equalsIgnoreCase("curr-asdf"));
        });
        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:curr+"));
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        milestoneQualifiers.stream().forEach(msQ -> {
            assertEquals(true, msQ.getContent().get().equalsIgnoreCase("curr+"));
        });
        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:current+"));
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        milestoneQualifiers.stream().forEach(msQ -> {
            assertEquals(true, msQ.getContent().get().equalsIgnoreCase("current+"));
        });
        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:current+4asdf"));
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        milestoneQualifiers.stream().forEach(msQ -> {
            assertEquals(true, msQ.getContent().get().equalsIgnoreCase("current+4asdf"));
        });

        // test disjunction and conjunction
        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model,
                Parser.parse("milestone:curr+2 || milestone:curr+3"));
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        assertEquals(false, milestoneQualifiers.get(0).getContent().get().equalsIgnoreCase("curr+2"));
        assertEquals(true, milestoneQualifiers.get(0).getContent().get().equalsIgnoreCase("V0.6"));

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model,
                Parser.parse("milestone:curr+2 && milestone:curr+3"));
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        assertEquals(false, milestoneQualifiers.get(0).isFalseQualifier());

    }

    @Test
    public void replaceMilestoneAliasesTestNoOpenMilestone() {
        // - expect next to last milestone (i.e. don't exist yet) to be set as current
        // - all milestones are closed and due in the future

        TurboMilestone msClose1 = new TurboMilestone(REPO, 1, "future1");
        msClose1.setOpen(false);
        msClose1.setDueDate(Optional.of(LocalDate.now().plusMonths(2)));

        TurboMilestone msClose2 = new TurboMilestone(REPO, 2, "future2");
        msClose2.setOpen(false);
        msClose2.setDueDate(Optional.of(LocalDate.now().plusMonths(3)));

        TurboMilestone msClose3 = new TurboMilestone(REPO, 3, "future3");
        msClose3.setOpen(false);
        msClose3.setDueDate(Optional.of(LocalDate.now().plusMonths(4)));

        model = TestUtils.singletonModel(new Model(REPO,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(Arrays.asList(msClose1, msClose2, msClose3)),
                new ArrayList<>()));

        assertMilestoneAliasFalseQualifierSize1("milestone:current");
        assertMilestoneAliasFalseQualifierSize1("milestone:current+1");

        FilterExpression noMilestoneAlias;
        List<Qualifier> milestoneQualifiers;

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse("milestone:current-1"));
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        milestoneQualifiers.stream().forEach(msQ -> {
            assertEquals(true, msQ.getContent().get().equalsIgnoreCase("future3"));
        });
    }

    public void assertMilestoneAliasFalseQualifierSize1(String filterText) {
        FilterExpression noMilestoneAlias;
        List<Qualifier> milestoneQualifiers;

        noMilestoneAlias = Qualifier.replaceMilestoneAliases(model, Parser.parse(filterText));
        assertEquals(noMilestoneAlias.getQualifierNames().size(), 1);
        assertEquals(noMilestoneAlias.getQualifierNames().get(0), "false");
        milestoneQualifiers = noMilestoneAlias.find(Qualifier::isMilestoneQualifier);
        assertEquals(milestoneQualifiers.size(), 0);
    }
}
