package service;

import static org.eclipse.egit.github.core.client.IGitHubConstants.CHARSET_UTF8;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * A wrapper class for GitHubEvents that also contain event-specific
 * information.
 */
public class GitHubEventsResponse {
	
	private GitHubResponse response;
	private ArrayList<TurboIssueEvent> turboIssueEvents;
	
	public GitHubEventsResponse(GitHubResponse response, InputStream jsonBody) {
		this.response = response;
		this.turboIssueEvents = new ArrayList<TurboIssueEvent>();
		parseEventParameters(jsonBody);
	}

	@SuppressWarnings("unchecked")
	private void parseEventParameters(InputStream jsonBody) {
		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, Object>[]>(){}.getType();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(jsonBody, CHARSET_UTF8));
			Map<String, Object>[] eventsWithParameters = gson.fromJson(reader, type);
			IssueEvent[] issueEvents = (IssueEvent[]) response.getBody();

			Map<String, String> parameters;
			for (int i=0; i<issueEvents.length; i++) {
				TurboIssueEvent event = new TurboIssueEvent(IssueEventType.fromString(issueEvents[i].getEvent()));

				switch (event.getType()) {
				case Renamed:
					// two string keys: from, to
					parameters = (Map<String, String>) eventsWithParameters[i].get("rename");
					for (String key : parameters.keySet()) {
						String x = parameters.get(key);
						event.getProperties().put(key, x);
					}
				    break;
				case Milestoned:
				case Demilestoned:
					// one string key: title
					parameters = (Map<String, String>) eventsWithParameters[i].get("milestone");
					for (String key : parameters.keySet()) {
						String x = parameters.get(key);
						event.getProperties().put(key, x);
					}
				    break;
				case Labeled:
				case Unlabeled:
					// two string keys: name, color (hex, without #)
					parameters = (Map<String, String>) eventsWithParameters[i].get("label");
					for (String key : parameters.keySet()) {
						String x = parameters.get(key);
						event.getProperties().put(key, x);
					}
				    break;
				case Assigned:
				case Unassigned:
					User assignee = (User) eventsWithParameters[i].get("assignee");
					break;
				case Closed:
				case Reopened:
				case Locked:
				case Unlocked:
					// No need to do anything
					break;
				case Subscribed:
				case Merged:
				case HeadRefDeleted:
				case HeadRefRestored:
				case Referenced:
				case Mentioned:
				default:
					// Not yet implemented, or no events triggered
				}
				turboIssueEvents.add(event);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public GitHubResponse getResponse() {
		return response;
	}

}
