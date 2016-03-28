package github;

import static org.eclipse.egit.github.core.client.IGitHubConstants.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.service.IssueService;

public class IssueServiceEx extends IssueService {

    private final GitHubClientEx ghClient;

    public IssueServiceEx(GitHubClientEx client) {
        super(client);
        this.ghClient = client;
    }

    private HttpURLConnection createIssuePostConnection(IRepositoryIdProvider repository,
                                                        int issueId) throws IOException {

        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(repository.generateId())
                .append(SEGMENT_ISSUES).append('/').append(issueId);
        return ghClient.createPost(uri.toString());
    }

    @Override
    public Issue createIssue(IRepositoryIdProvider repository, Issue issue) throws IOException {
        Issue returnedIssue = super.createIssue(repository, issue);
        if (!returnedIssue.getState().equals(issue.getState())) {
            returnedIssue.setState(issue.getState());
            editIssueState(repository, returnedIssue.getNumber(),
                           returnedIssue.getState().equals(STATE_OPEN));
        }
        return returnedIssue;
    }

    public Issue editIssueState(IRepositoryIdProvider repository, int issueId, boolean isOpen) throws IOException {
        HttpURLConnection connection = createIssuePostConnection(repository, issueId);
        HashMap<Object, Object> data = new HashMap<>();
        String state = isOpen ? STATE_OPEN : STATE_CLOSED;
        data.put(FILTER_STATE, state);
        return ghClient.sendJson(connection, data, Issue.class);
    }

    /**
     * Retrieves a list of issue events together with the new ETag if the events are updated,
     * and an empty list with the current ETag if there are no new events.
     *
     * @param repository The repository from which to retrieve the issue
     * @param issueId    The numeric ID of the issue
     * @param eTag       The eTag to be added to the request header
     * @return list of issue events
     * @throws IOException
     */
    public GitHubEventsResponse getIssueEvents(IRepositoryIdProvider repository, int issueId, String eTag)
            throws IOException {
        GitHubRequest request = createRequest();
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(repository.generateId())
                .append(SEGMENT_ISSUES).append('/').append(issueId)
                .append(SEGMENT_EVENTS);
        request.setUri(uri);
        request.setType(IssueEvent[].class);
        return ghClient.getEvent(request, eTag);
    }
}
