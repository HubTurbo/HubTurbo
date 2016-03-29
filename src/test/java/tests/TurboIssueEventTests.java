package tests;

import static org.junit.Assert.assertEquals;

import github.IssueEventType;
import github.TurboIssueEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.User;
import org.junit.Test;
import util.Utility;

public class TurboIssueEventTests {

    public List<TurboIssueEvent> sampleEvents;

    private static TurboIssueEvent createLabelUpdateEvent(
            String userName, IssueEventType eventType,
            LocalDateTime time, String labelName, String labelColour) {

        return new TurboIssueEvent(
                new User().setLogin(userName), eventType, Utility.localDateTimeToDate(time))
                .setLabelName(labelName).setLabelColour(labelColour);
    }

    public TurboIssueEventTests() {
        sampleEvents = new ArrayList<>();

        sampleEvents.add(
                createLabelUpdateEvent("A", IssueEventType.Labeled,
                                       LocalDateTime.of(2015, 1, 1, 1, 1, 0),
                                       "A1", "aaaaaa"));
        sampleEvents.add(
                createLabelUpdateEvent("A", IssueEventType.Unlabeled,
                                       LocalDateTime.of(2015, 1, 1, 1, 2, 0),
                                       "A2", "aaaaaa"));
        sampleEvents.add(
                createLabelUpdateEvent("B", IssueEventType.Labeled,
                                       LocalDateTime.of(2015, 1, 1, 1, 0, 30),
                                       "B1", "bbbbbb"));
        sampleEvents.add(
                createLabelUpdateEvent("B", IssueEventType.Labeled,
                                       LocalDateTime.of(2015, 1, 1, 1, 1, 0),
                                       "B2", "bbbbbb"));
        sampleEvents.add(
                createLabelUpdateEvent("B", IssueEventType.Unlabeled,
                                       LocalDateTime.of(2015, 1, 1, 1, 1, 31),
                                       "B1", "bbbbbb"));
        sampleEvents.add(
                createLabelUpdateEvent("C", IssueEventType.Labeled,
                                       LocalDateTime.of(2015, 1, 1, 2, 30, 15),
                                       "C1", "cccccc"));
        sampleEvents.add(
                createLabelUpdateEvent("D", IssueEventType.Unlabeled,
                                       LocalDateTime.of(2015, 1, 1, 3, 20, 59),
                                       "D1", "dddddd"));
    }

    /**
     * The method groupLabelUpdateEvents should not modify the input events list
     */
    @Test
    public void testLabelUpdateEventsGroupingInvariant() {
        List<TurboIssueEvent> events = new ArrayList<>(sampleEvents);
        TurboIssueEvent.groupLabelUpdateEvents(events);
        assertEquals(sampleEvents, events);
    }

    /**
     * Tests the events grouping when list of events is empty
     */
    @Test
    public void testLabelUpdateEventsGroupingForNoEvent() {
        assertEquals(new ArrayList<TurboIssueEvent>(),
                     TurboIssueEvent.groupLabelUpdateEvents(new ArrayList<TurboIssueEvent>()));
    }

    /**
     * Tests the events grouping when there is only 1 event
     */
    @Test
    public void testLabelUpdateEventsGroupingForOneEvent() {
        List<TurboIssueEvent> events = new ArrayList<>();
        events.add(sampleEvents.get(0));

        List<TurboIssueEvent> expectedSubList1 = new ArrayList<>();
        expectedSubList1.add(sampleEvents.get(0));

        List<List<TurboIssueEvent>> expected = new ArrayList<>();
        expected.add(expectedSubList1);

        assertEquals(expected, TurboIssueEvent.groupLabelUpdateEvents(events));
    }

    /**
     * Tests when A and B update the overlapping set of labels
     * at overlapping time. The order of modification are
     * A:+L1 -> B:-L1 -> A:+L2 -> B:+L2
     * Expected: [A:[+L1, +L2], B:[-L1, +L2]]
     * Note: This scenario may not actually occur since A and B
     * might not be recorded as adding the same label L2 by github
     */
    @Test
    public void testLabelUpdateEventsGrouping1() {
        List<TurboIssueEvent> events = new ArrayList<>();

        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 0),
                                          "L1", "ffffff"));
        events.add(createLabelUpdateEvent("B", IssueEventType.Unlabeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 25),
                                          "L1", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 40),
                                          "L2", "ffffff"));
        events.add(createLabelUpdateEvent("B", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 2, 11),
                                          "L2", "ffffff"));

        List<TurboIssueEvent> expectedSubList1 = new ArrayList<>();
        expectedSubList1.add(events.get(0));
        expectedSubList1.add(events.get(2));

        List<TurboIssueEvent> expectedSubList2 = new ArrayList<>();
        expectedSubList2.add(events.get(1));
        expectedSubList2.add(events.get(3));

        List<List<TurboIssueEvent>> expected = new ArrayList<>();
        expected.add(expectedSubList1);
        expected.add(expectedSubList2);

        assertEquals(expected, TurboIssueEvent.groupLabelUpdateEvents(events));
    }

    /**
     * Tests when A and B update at exactly the same time
     * at overlapping time. The order of modification are
     * (A:+L1 = B:-L1) -> (A:+L2 = B:+L2)
     * Expected: [A:[+L1, +L2], B:[-L1, +L2]] according
     * to lexicographic order since Collections.sort is stable
     */
    @Test
    public void testLabelUpdateEventsGrouping2() {
        List<TurboIssueEvent> events = new ArrayList<>();

        events.add(createLabelUpdateEvent("B", IssueEventType.Unlabeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 0),
                                          "L1", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 0),
                                          "L1", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 40),
                                          "L2", "ffffff"));
        events.add(createLabelUpdateEvent("B", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 40),
                                          "L2", "ffffff"));

        List<TurboIssueEvent> expectedSubList1 = new ArrayList<>();
        expectedSubList1.add(events.get(1));
        expectedSubList1.add(events.get(2));

        List<TurboIssueEvent> expectedSubList2 = new ArrayList<>();
        expectedSubList2.add(events.get(0));
        expectedSubList2.add(events.get(3));

        List<List<TurboIssueEvent>> expected = new ArrayList<>();
        expected.add(expectedSubList1);
        expected.add(expectedSubList2);

        assertEquals(expected, TurboIssueEvent.groupLabelUpdateEvents(events));
    }

    /**
     * Tests when A modify same set of labels multiple times
     * A: +L1, +L1, -L1, +L2, +L3, -L2, +L2
     * Expected: [[+L1, +L1, -L1, +L2, +L3, -L2, +L2]] since
     * all events are reflected and groupLabelUpdateEvents does not
     * attempt to repetitive labels
     */
    @Test
    public void testLabelUpdateEventsGrouping3() {
        List<TurboIssueEvent> events = new ArrayList<>();

        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 0),
                                          "L1", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 5),
                                          "L1", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Unlabeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 10),
                                          "L1", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 15),
                                          "L2", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 15),
                                          "L3", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Unlabeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 25),
                                          "L2", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 35),
                                          "L2", "cccccc"));

        List<List<TurboIssueEvent>> expected = new ArrayList<>();
        expected.add(events);

        assertEquals(expected, TurboIssueEvent.groupLabelUpdateEvents(events));
    }

    /**
     * Tests when A modify a set of labels but are separated into
     * 3 groups due to time stamp's difference
     * A: +L1, +L1, -L1, +L2, +L3, -L2, +L2
     * Expected: [[+L1, +L1, -L1, +L2], [+L3, -L2], [+L2]]
     */
    @Test
    public void testLabelUpdateEventsGrouping4() {
        List<TurboIssueEvent> events = new ArrayList<>();

        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 0),
                                          "L1", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 5),
                                          "L1", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Unlabeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 10),
                                          "L1", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 15),
                                          "L2", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 2, 15),
                                          "L3", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Unlabeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 2, 45),
                                          "L2", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 5, 35),
                                          "L2", "ffffff"));

        List<TurboIssueEvent> expectedSubList1 = new ArrayList<>();
        expectedSubList1.add(events.get(0));
        expectedSubList1.add(events.get(1));
        expectedSubList1.add(events.get(2));
        expectedSubList1.add(events.get(3));

        List<TurboIssueEvent> expectedSubList2 = new ArrayList<>();
        expectedSubList2.add(events.get(4));
        expectedSubList2.add(events.get(5));

        List<TurboIssueEvent> expectedSubList3 = new ArrayList<>();
        expectedSubList3.add(events.get(6));

        List<List<TurboIssueEvent>> expected = new ArrayList<>();
        expected.add(expectedSubList1);
        expected.add(expectedSubList2);
        expected.add(expectedSubList3);

        assertEquals(expected, TurboIssueEvent.groupLabelUpdateEvents(events));
    }

    /**
     * Tests a generic events list with several corner cases:
     * All events are not in chronological order in the input events list
     * A has 2 events exactly 1 minute from each other
     * B has 3 events 2 of which are 30s part
     * and the 3rd one is 61s apart from the 1st one
     * B's first 2 events overlap with A's events but B's 1st event occurs first
     * C and D have completely separate events
     * Expected grouping: [[B1, B2], [A1, A2], [B3], [C1], [D1]]
     */
    @Test
    public void testLabelUpdateEventsGrouping5() {
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

    /**
     * Tests when A and B update the overlapping set of labels
     * immediately following each other. The order of modification are
     * A:+L1 -> A:+L2 -> B:-L1 -> B:-L2
     * Expected: [A:[+L1, +L2], B:[-L1, -L2]]
     */
    @Test
    public void testLabelUpdateEventsGrouping6() {
        List<TurboIssueEvent> events = new ArrayList<>();

        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 0),
                                          "L1", "ffffff"));
        events.add(createLabelUpdateEvent("B", IssueEventType.Unlabeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 40),
                                          "L1", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 40),
                                          "L2", "ffffff"));
        events.add(createLabelUpdateEvent("B", IssueEventType.Unlabeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 2, 0),
                                          "L2", "ffffff"));

        List<TurboIssueEvent> expectedSubList1 = new ArrayList<>();
        expectedSubList1.add(events.get(0));
        expectedSubList1.add(events.get(2));

        List<TurboIssueEvent> expectedSubList2 = new ArrayList<>();
        expectedSubList2.add(events.get(1));
        expectedSubList2.add(events.get(3));

        List<List<TurboIssueEvent>> expected = new ArrayList<>();
        expected.add(expectedSubList1);
        expected.add(expectedSubList2);

        assertEquals(expected, TurboIssueEvent.groupLabelUpdateEvents(events));
    }

    /**
     * Tests when A and B and C update at overlapping times but
     * C's events appear before A and B in the input events List
     * A's events are too far apart to be considered in the same group
     * A1 -> C1 -> A2 -> B1 -> C2 -> B2
     * Expected: [[A1], [A2], [C1, C2], [B1, B2]]
     */
    @Test
    public void testLabelUpdateEventsGrouping7() {
        List<TurboIssueEvent> events = new ArrayList<>();

        events.add(createLabelUpdateEvent("C", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 5, 0),
                                          "C1", "cccccc"));
        events.add(createLabelUpdateEvent("B", IssueEventType.Unlabeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 5, 40),
                                          "B1", "bbbbbb"));
        events.add(createLabelUpdateEvent("C", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 5, 59),
                                          "C2", "eeeeee"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Unlabeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 4, 55),
                                          "A2", "aaaaaa"));
        events.add(createLabelUpdateEvent("B", IssueEventType.Unlabeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 6, 15),
                                          "B2", "bbbbbb"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 3, 40),
                                          "A1", "aaaaaa"));

        List<TurboIssueEvent> expectedSubList1 = new ArrayList<>();
        expectedSubList1.add(events.get(5));

        List<TurboIssueEvent> expectedSubList2 = new ArrayList<>();
        expectedSubList2.add(events.get(3));

        List<TurboIssueEvent> expectedSubList3 = new ArrayList<>();
        expectedSubList3.add(events.get(0));
        expectedSubList3.add(events.get(2));

        List<TurboIssueEvent> expectedSubList4 = new ArrayList<>();
        expectedSubList4.add(events.get(1));
        expectedSubList4.add(events.get(4));

        List<List<TurboIssueEvent>> expected = new ArrayList<>();
        expected.add(expectedSubList1);
        expected.add(expectedSubList2);
        expected.add(expectedSubList3);
        expected.add(expectedSubList4);

        assertEquals(expected, TurboIssueEvent.groupLabelUpdateEvents(events));
    }

    /**
     * Tests when A modify labels at 61s away from each other
     * A: +L1, -L1
     * Expected: [[+L1], [-L1]]
     */
    @Test
    public void testLabelUpdateEventsGrouping8() {
        List<TurboIssueEvent> events = new ArrayList<>();

        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 0),
                                          "L1", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Unlabeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 2, 1),
                                          "L2", "ffffff"));

        List<TurboIssueEvent> expectedSubList1 = new ArrayList<>();
        expectedSubList1.add(events.get(0));

        List<TurboIssueEvent> expectedSubList2 = new ArrayList<>();
        expectedSubList2.add(events.get(1));

        List<List<TurboIssueEvent>> expected = new ArrayList<>();
        expected.add(expectedSubList1);
        expected.add(expectedSubList2);

        assertEquals(expected, TurboIssueEvent.groupLabelUpdateEvents(events));
    }

    /**
     * Tests when A modify labels at 60s away from each other
     * A: +L1, -L1
     * Expected: [[+L1], [-L1]]
     */
    @Test
    public void testLabelUpdateEventsGrouping9() {
        List<TurboIssueEvent> events = new ArrayList<>();

        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 0),
                                          "L1", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Unlabeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 2, 0),
                                          "L2", "ffffff"));

        List<TurboIssueEvent> expectedSubList1 = new ArrayList<>();
        expectedSubList1.add(events.get(0));
        expectedSubList1.add(events.get(1));

        List<List<TurboIssueEvent>> expected = new ArrayList<>();
        expected.add(expectedSubList1);

        assertEquals(expected, TurboIssueEvent.groupLabelUpdateEvents(events));
    }

    /**
     * Tests when A modify labels at 59s away from each other
     * A: +L1, -L1
     * Expected: [[+L1], [-L1]]
     */
    @Test
    public void testLabelUpdateEventsGrouping10() {
        List<TurboIssueEvent> events = new ArrayList<>();

        events.add(createLabelUpdateEvent("A", IssueEventType.Labeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 1, 0),
                                          "L1", "ffffff"));
        events.add(createLabelUpdateEvent("A", IssueEventType.Unlabeled,
                                          LocalDateTime.of(2015, 1, 1, 1, 2, 0),
                                          "L2", "ffffff"));

        List<TurboIssueEvent> expectedSubList1 = new ArrayList<>();
        expectedSubList1.add(events.get(0));
        expectedSubList1.add(events.get(1));

        List<List<TurboIssueEvent>> expected = new ArrayList<>();
        expected.add(expectedSubList1);

        assertEquals(expected, TurboIssueEvent.groupLabelUpdateEvents(events));
    }
}
