package github;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.eclipse.egit.github.core.client.IGitHubConstants.CHARSET_UTF8;

/**
 * A wrapper class for GitHubEvents that also contain event-specific
 * information.
 */
public class GitHubEventsResponse {

    private static final Logger logger = LogManager.getLogger(GitHubEventsResponse.class.getName());

    private final GitHubResponse response;
    private final List<TurboIssueEvent> turboIssueEvents;
    private final String updatedETag;

    public GitHubEventsResponse(GitHubResponse response, InputStream jsonBody, String updatedETag) {
        this.response = response;
        this.turboIssueEvents = parseEventParameters(jsonBody);
        this.updatedETag = updatedETag;
    }

    @SuppressWarnings("unchecked")
    private List<TurboIssueEvent> parseEventParameters(InputStream jsonBody) {
        ArrayList<TurboIssueEvent> eventsInJson = new ArrayList<>();

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>[]>() {}.getType();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new InputStreamReader(jsonBody, CHARSET_UTF8));

            Map<String, Object>[] eventsWithParameters = gson.fromJson(reader, type);

            if (eventsWithParameters == null) {
                return eventsInJson;
            }

            IssueEvent[] issueEvents = (IssueEvent[]) response.getBody();

            Map<String, String> parameters;
            for (int i = 0; i < issueEvents.length; i++) {
                TurboIssueEvent event = new TurboIssueEvent(
                        issueEvents[i].getActor(),
                        IssueEventType.fromString(issueEvents[i].getEvent()),
                        issueEvents[i].getCreatedAt());

                switch (event.getType()) {
                case Renamed:
                    // two string keys: from, to
                    parameters = (Map<String, String>) eventsWithParameters[i].get("rename");
                    event.setRenamedFrom(parameters.get("from"));
                    event.setRenamedTo(parameters.get("to"));
                    break;
                case Milestoned:
                case Demilestoned:
                    // one string key: title
                    parameters = (Map<String, String>) eventsWithParameters[i].get("milestone");
                    event.setMilestoneTitle(parameters.get("title"));
                    break;
                case Labeled:
                case Unlabeled:
                    // two string keys: name, color (hex, without #)
                    parameters = (Map<String, String>) eventsWithParameters[i].get("label");
                    event.setLabelColour(parameters.get("color"));
                    event.setLabelName(parameters.get("name"));
                    break;
                case Assigned:
                case Unassigned:
                    // User object
                    // re-serialise it using Gson
                    Object assigneeMap = eventsWithParameters[i].get("assignee");
                    String json = new Gson().toJson(assigneeMap);
                    User user = gson.fromJson(json, User.class);
                    event.setAssignedUser(user);
                    break;
                case Closed:
                case Reopened:
                case Locked:
                case Unlocked:
                    // No need to do anything
                    break;
                case Subscribed:
                case Unsubscribed:
                case Merged:
                case HeadRefDeleted:
                case HeadRefRestored:
                case Referenced:
                case Mentioned:
                default:
                    // Not yet implemented, or no events triggered
                }
                eventsInJson.add(event);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return eventsInJson;
    }

    public List<TurboIssueEvent> getTurboIssueEvents() {
        return turboIssueEvents;
    }

    public String getUpdatedETag() {
        return updatedETag;
    }
}
