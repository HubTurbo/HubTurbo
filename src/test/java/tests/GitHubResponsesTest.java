package tests;

import github.GitHubEventsResponse;
import github.IssueEventType;
import github.TurboIssueEvent;
import org.apache.commons.io.IOUtils;
import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Ensures the conversion from GitHub responses' body to HubTurbo resource objects are correct.
 */
public class GitHubResponsesTest {

    @Test
    public void gitHubEventsResponseConstructorTest() throws IOException {
        // Must match the details in eventsResponseJson of gitHubEventsResponseJsonStream.
        IssueEvent testNonSelfEvent = new IssueEvent()
                .setActor(new User().setLogin("test-nonself"))
                .setCreatedAt(new Date())
                .setEvent("renamed");
        IssueEvent testSelfEvent = new IssueEvent()
                .setActor(new User().setLogin("test"))
                .setCreatedAt(new Date())
                .setEvent("milestoned");
        IssueEvent[] testEvents = { testNonSelfEvent, testSelfEvent };

        // We parse the input stream string here.
        GitHubResponse testResponse = new GitHubResponse(null, testEvents);
        GitHubEventsResponse testEventsResponse = new GitHubEventsResponse(testResponse,
                                                                           gitHubEventsResponseJsonStream(),
                                                                           "");
        List<TurboIssueEvent> issueEvents = testEventsResponse.getTurboIssueEvents();

        // Will fail if the GitHubEventsResponse constructor doesn't parse properly.
        assertEquals(2, issueEvents.size());
        assertEquals(IssueEventType.Renamed, issueEvents.get(0).getType());
        assertEquals("test issue 1", issueEvents.get(0).getRenamedFrom());
        assertEquals("test issue 1.1", issueEvents.get(0).getRenamedTo());
        assertEquals(IssueEventType.Milestoned, issueEvents.get(1).getType());
        assertEquals("3.0.0", issueEvents.get(1).getMilestoneTitle());
    }

    /**
     * Stripped-down version of a GitHub events response body e.g. when you GET
     * /repos/HubTurbo/HubTurbo/issues/1/events.
     *
     * @return The response body as an InputStream
     * @throws IOException Thrown by IOUtils in converting the String to an InputStream
     */
    private InputStream gitHubEventsResponseJsonStream() throws IOException {
        String eventsResponseJson =
                "[{\"actor\":{\"login\":\"test-nonself\"},\"event\":\"renamed\"," +
                        "\"created_at\":\"2015-06-12T12:09:07\"," +
                        "\"rename\":{\"from\":\"test issue 1\",\"to\":\"test issue 1.1\"}}," +
                        "{\"actor\":{\"login\":\"test\"},\"event\":\"milestoned\"," +
                        "\"created_at\":\"2015-06-12T02:24:14Z\",\"milestone\":{\"title\":\"3.0.0\"}}]";

        return IOUtils.toInputStream(eventsResponseJson, "UTF-8");
    }

}
