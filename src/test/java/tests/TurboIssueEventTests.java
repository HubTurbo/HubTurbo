package tests;

import static org.junit.Assert.assertEquals;
import github.IssueEventType;
import github.TurboIssueEvent;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.egit.github.core.User;
import org.junit.Test;

public class TurboIssueEventTests {

    private List<TurboIssueEvent> sampleEvents;

    public TurboIssueEventTests() {
        sampleEvents = new ArrayList<>();

        sampleEvents.add(
            new TurboIssueEvent(
                new User().setLogin("A"), IssueEventType.Labeled,
                new GregorianCalendar(2015, 1, 1, 1, 1, 0).getTime())
            .setLabelName("A1"));
        sampleEvents.add(
            new TurboIssueEvent(
                new User().setLogin("A"), IssueEventType.Unlabeled,
                new GregorianCalendar(2015, 1, 1, 1, 2, 0).getTime())
            .setLabelName("A2"));

        sampleEvents.add(
            new TurboIssueEvent(
                new User().setLogin("B"), IssueEventType.Labeled,
                new GregorianCalendar(2015, 1, 1, 1, 0, 30).getTime())
            .setLabelName("B1"));
        sampleEvents.add(
            new TurboIssueEvent(
                new User().setLogin("B"), IssueEventType.Labeled,
                new GregorianCalendar(2015, 1, 1, 1, 1, 0).getTime())
            .setLabelName("B2"));
        sampleEvents.add(
            new TurboIssueEvent(
                new User().setLogin("B"), IssueEventType.Unlabeled,
                new GregorianCalendar(2015, 1, 1, 1, 1, 31).getTime())
            .setLabelName("B1"));

        sampleEvents.add(
            new TurboIssueEvent(
                new User().setLogin("C"), IssueEventType.Labeled,
                new GregorianCalendar(2015, 1, 1, 2, 30, 15).getTime())
            .setLabelName("C1"));


        sampleEvents.add(
            new TurboIssueEvent(
                new User().setLogin("D"), IssueEventType.Unlabeled,
                new GregorianCalendar(2015, 1, 1, 3, 20, 59).getTime())
            .setLabelName("D1"));
    }

    @Test
    public void testLabelUpdateEventsGroupingNoEvent() {
        assertEquals(new ArrayList<TurboIssueEvent>(),
                     TurboIssueEvent.groupLabelUpdateEvents(new ArrayList<TurboIssueEvent>()));
    }

    @Test
    public void testLabelUpdateEventsGroupingOneEvent() {
        List<TurboIssueEvent> events = new ArrayList<>();
        events.add(sampleEvents.get(0));

        List<TurboIssueEvent> expectedSubList1 = new ArrayList<>();
        expectedSubList1.add(sampleEvents.get(0));

        List<List<TurboIssueEvent>> expected = new ArrayList<>();
        expected.add(expectedSubList1);

        assertEquals(expected,
                     TurboIssueEvent.groupLabelUpdateEvents(events));
    }

    @Test
    public void testLabelUpdateEventsGroupingSampleEvents() {
        List<TurboIssueEvent> events = new ArrayList<>(sampleEvents);

        List<TurboIssueEvent> expectedSubList1 = new ArrayList<>();
        expectedSubList1.add(sampleEvents.get(2));
        expectedSubList1.add(sampleEvents.get(3));

        List<TurboIssueEvent> expectedSubList2 = new ArrayList<>();
        expectedSubList2.add(sampleEvents.get(0));
        expectedSubList2.add(sampleEvents.get(1));

        List<TurboIssueEvent> expectedSubList3 = new ArrayList<>();
        expectedSubList3.add(sampleEvents.get(4));

        List<TurboIssueEvent> expectedSubList4 = new ArrayList<>();
        expectedSubList4.add(sampleEvents.get(5));

        List<TurboIssueEvent> expectedSubList5 = new ArrayList<>();
        expectedSubList5.add(sampleEvents.get(6));

        List<List<TurboIssueEvent>> expected = new ArrayList<>();
        expected.add(expectedSubList1);
        expected.add(expectedSubList2);
        expected.add(expectedSubList3);
        expected.add(expectedSubList4);
        expected.add(expectedSubList5);

        assertEquals(expected,
                     TurboIssueEvent.groupLabelUpdateEvents(events));
    }
}
