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

    public List<TurboIssueEvent> sampleEvents;

    public static TurboIssueEvent createLabelUpdateEvent(
            String userName, IssueEventType eventType,
            GregorianCalendar time, String labelName) {

        return new TurboIssueEvent(
                   new User().setLogin(userName), eventType, time.getTime())
               .setLabelName(labelName);
    }

    public TurboIssueEventTests() {
        sampleEvents = new ArrayList<>();

        sampleEvents.add(
                createLabelUpdateEvent("A", IssueEventType.Labeled,
                        new GregorianCalendar(2015, 1, 1, 1, 1, 0),
                        "A1"));
        sampleEvents.add(
                createLabelUpdateEvent("A", IssueEventType.Unlabeled,
                        new GregorianCalendar(2015, 1, 1, 1, 2, 0),
                        "A2"));
        sampleEvents.add(
                createLabelUpdateEvent("B", IssueEventType.Labeled,
                        new GregorianCalendar(2015, 1, 1, 1, 0, 30),
                        "B1"));
        sampleEvents.add(
                createLabelUpdateEvent("B", IssueEventType.Labeled,
                        new GregorianCalendar(2015, 1, 1, 1, 1, 0),
                        "B2"));
        sampleEvents.add(
                createLabelUpdateEvent("B", IssueEventType.Unlabeled,
                        new GregorianCalendar(2015, 1, 1, 1, 1, 31),
                        "B1"));
        sampleEvents.add(
                createLabelUpdateEvent("C", IssueEventType.Labeled,
                        new GregorianCalendar(2015, 1, 1, 2, 30, 15),
                        "C1"));
        sampleEvents.add(
                createLabelUpdateEvent("D", IssueEventType.Unlabeled,
                        new GregorianCalendar(2015, 1, 1, 3, 20, 59),
                        "D1"));
    }

    @Test
    public void testLabelUpdateEventsGroupingForNoEvent() {
        assertEquals(new ArrayList<TurboIssueEvent>(),
                     TurboIssueEvent.groupLabelUpdateEvents(new ArrayList<TurboIssueEvent>()));
    }

    @Test
    public void testLabelUpdateEventsGroupingForOneEvent() {
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
    public void testLabelUpdateEventsGroupingForSampleEvents() {
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
