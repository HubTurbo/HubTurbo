package tests;

import static junit.framework.Assert.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.User;
import org.junit.Test;

import backend.IssueMetadata;
import github.IssueEventType;
import github.TurboIssueEvent;
import util.Utility;

public class IssueMetadataTests {

    @Test
    public void immutability() {
        List<TurboIssueEvent> events = stubEvents();
        List<Comment> comments = stubComments();

        IssueMetadata metadata = new IssueMetadata(events, comments, "", "");
        assertEquals(3, metadata.getEvents().size());
        assertEquals(3, metadata.getComments().size());

        events.addAll(stubEvents());
        comments.addAll(stubComments());

        assertEquals(3, metadata.getEvents().size());
        assertEquals(3, metadata.getComments().size());

        metadata.getEvents().addAll(stubEvents());
        metadata.getComments().addAll(stubComments());

        assertEquals(3, metadata.getEvents().size());
        assertEquals(3, metadata.getComments().size());
    }

    @Test
    public void empty() {
        IssueMetadata empty = new IssueMetadata();
        assertEquals(new ArrayList<TurboIssueEvent>(), empty.getEvents());
        assertEquals(new ArrayList<Comment>(), empty.getComments());
        assertEquals(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0)), empty.getNonSelfUpdatedAt());
        assertEquals(0, empty.getNonSelfCommentCount());
        assertEquals("", empty.getEventsETag());
        assertEquals("", empty.getCommentsETag());
        assertEquals(false, empty.isLatest());
    }

    @Test
    public void invalidation() {
        IssueMetadata empty = new IssueMetadata();
        IssueMetadata trueMetadata = new IssueMetadata(empty, true);
        assertEquals(true, trueMetadata.isLatest());
    }

    @Test
    public void intermediate() {
        IssueMetadata metadata = new IssueMetadata(stubEvents(), stubComments(), "events", "comments");
        assertEquals(3, metadata.getEvents().size());
        assertEquals(3, metadata.getComments().size());
        assertEquals("events", metadata.getEventsETag());
        assertEquals("comments", metadata.getCommentsETag());

        // Computed properties are empty
        assertEquals(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0)), metadata.getNonSelfUpdatedAt());
        assertEquals(0, metadata.getNonSelfCommentCount());
    }

    @Test
    public void computed() {
        IssueMetadata original = new IssueMetadata(stubEvents(), stubComments(), "events", "comments");

        // Computed properties are empty
        assertEquals(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0)), original.getNonSelfUpdatedAt());
        assertEquals(0, original.getNonSelfCommentCount());

        // They are no longer empty
        IssueMetadata computed = new IssueMetadata(original, "test");
        assertEquals(Utility.dateToLocalDateTime(now), computed.getNonSelfUpdatedAt());
        assertEquals(2, computed.getNonSelfCommentCount());
    }

    @Test
    public void update() {
//        public IssueMetadata(IssueMetadata other, LocalDateTime nonSelfUpdatedAt,
//            List<TurboIssueEvent> currEvents, String currEventsETag) {

        List<TurboIssueEvent> originalEvents = stubEvents();
        List<Comment> originalComments = stubComments();

        IssueMetadata original = new IssueMetadata(originalEvents, originalComments, "events", "comments");
        IssueMetadata derived = new IssueMetadata(original, "test");

        assertEquals(originalEvents, derived.getEvents());
        assertEquals(originalComments, derived.getComments());
        assertEquals("events", derived.getEventsETag());
        assertEquals("comments", derived.getCommentsETag());

        // Computed properties are non-empty
        assertEquals(Utility.dateToLocalDateTime(now), derived.getNonSelfUpdatedAt());
        assertEquals(2, derived.getNonSelfCommentCount());

        LocalDateTime rightNow = LocalDateTime.now();

        // Failed update
        List<TurboIssueEvent> newEvents = stubEvents();
        IssueMetadata updated = new IssueMetadata(derived, rightNow, newEvents, "events2");

        assertEquals(originalEvents, updated.getEvents());
        assertEquals(originalComments, updated.getComments());
        assertEquals("events2", updated.getEventsETag());
        assertEquals("comments", updated.getCommentsETag());

        // Successful update
        updated = new IssueMetadata(derived, rightNow, newEvents, "events");

        assertEquals(newEvents, updated.getEvents());
        assertEquals(originalComments, updated.getComments());
        assertEquals("events", updated.getEventsETag());
        assertEquals("comments", updated.getCommentsETag());
    }

    private static final Date now = new Date();

    private static List<TurboIssueEvent> stubEvents() {
        List<TurboIssueEvent> events = new ArrayList<>();
        events.add(new TurboIssueEvent(new User().setLogin("test"), IssueEventType.Closed, now));
        events.add(new TurboIssueEvent(new User().setLogin("test-nonself"), IssueEventType.Closed, now));
        events.add(new TurboIssueEvent(new User().setLogin("test-nonself"), IssueEventType.Assigned, now));
        return events;
    }

    private static List<Comment> stubComments() {
        List<Comment> comments = new ArrayList<>();

        Comment comment = new Comment();
        comment.setBody("hello");
        comment.setUser(new User().setLogin("test"));
        comment.setCreatedAt(now);
        comments.add(comment);

        comment = new Comment();
        comment.setBody("not by me");
        comment.setUser(new User().setLogin("test-nonself"));
        comment.setCreatedAt(now);
        comments.add(comment);

        comment = new Comment();
        comment.setBody("not by me 2");
        comment.setUser(new User().setLogin("test-nonself"));
        comment.setCreatedAt(now);
        comments.add(comment);

        return comments;
    }

}
