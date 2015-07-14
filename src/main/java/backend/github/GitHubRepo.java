package backend.github;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.*;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.MilestoneService;

import backend.UserCredentials;
import backend.interfaces.Repo;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import github.GitHubClientExtended;
import github.IssueServiceExtended;
import github.LabelServiceFixed;
import github.TurboIssueEvent;
import github.update.*;
import ui.UI;
import util.HTLog;
import util.events.UpdateProgressEvent;

public class GitHubRepo implements Repo {

    private static final Logger logger = HTLog.get(GitHubRepo.class);

    private final GitHubClientExtended client = new GitHubClientExtended();
    private final IssueServiceExtended issueService = new IssueServiceExtended(client);
    private final CollaboratorService collaboratorService = new CollaboratorService(client);
    private final LabelServiceFixed labelService = new LabelServiceFixed(client);
    private final MilestoneService milestoneService = new MilestoneService(client);

    public GitHubRepo() {
    }

    @Override
    public boolean login(UserCredentials credentials) {
        client.setCredentials(credentials.username, credentials.password);

        // Attempt login
        try {
            GitHubRequest request = new GitHubRequest();
            request.setUri("/");
            client.get(request);
        } catch (IOException e) {
            // Login failed
            return false;
        }
        return true;
    }

    @Override
    public ImmutableTriple<List<TurboIssue>, String, Date> getUpdatedIssues(String repoId,
                                                                            String eTag, Date lastCheckTime) {

        IssueUpdateService issueUpdateService = new IssueUpdateService(client, eTag, lastCheckTime);
        List<Issue> updatedItems = issueUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId));
        List<TurboIssue> items = updatedItems.stream()
            .map(i -> new TurboIssue(repoId, i))
            .collect(Collectors.toList());
        return new ImmutableTriple<>(items, issueUpdateService.getUpdatedETag(),
            issueUpdateService.getUpdatedCheckTime());
    }

    @Override
    public ImmutablePair<List<TurboLabel>, String> getUpdatedLabels(String repoId, String eTag) {
        return getUpdatedResource(repoId, eTag, LabelUpdateService::new, TurboLabel::new);
    }

    @Override
    public ImmutablePair<List<TurboMilestone>, String> getUpdatedMilestones(String repoId, String eTag) {
        return getUpdatedResource(repoId, eTag, MilestoneUpdateService::new, TurboMilestone::new);
    }

    @Override
    public ImmutablePair<List<TurboUser>, String> getUpdatedCollaborators(String repoId, String eTag) {
        return getUpdatedResource(repoId, eTag, UserUpdateService::new, TurboUser::new);
    }

    private <TR, R, S extends UpdateService<R>> ImmutablePair<List<TR>, String> getUpdatedResource(
        String repoId, String eTag, BiFunction<GitHubClientExtended, String, S> constructService,
        BiFunction<String, R, TR> resourceConstructor) {

        S updateService = constructService.apply(client, eTag);
        List<R> updatedItems = updateService.getUpdatedItems(RepositoryId.createFromId(repoId));
        List<TR> items = updatedItems.stream()
            .map(i -> resourceConstructor.apply(repoId, i))
            .collect(Collectors.toList());
        return new ImmutablePair<>(items,
            updateService.getUpdatedETag());
    }

    @Override
    public List<TurboLabel> getLabels(String repoId) {
        try {
            return labelService.getLabels(RepositoryId.createFromId(repoId)).stream()
                .map(l -> new TurboLabel(repoId, l))
                .collect(Collectors.toList());
        } catch (IOException e) {
            HTLog.error(logger, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<TurboMilestone> getMilestones(String repoId) {
        try {
            return milestoneService.getMilestones(RepositoryId.createFromId(repoId), "all").stream()
                .map(m -> new TurboMilestone(repoId, m))
                .collect(Collectors.toList());
        } catch (IOException e) {
            HTLog.error(logger, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<TurboUser> getCollaborators(String repoId) {
        try {
            return collaboratorService.getCollaborators(RepositoryId.createFromId(repoId)).stream()
                .map(u -> new TurboUser(repoId, u))
                .collect(Collectors.toList());
        } catch (RequestException e) {
            if (e.getStatus() == 403) {
                logger.info(HTLog.format(repoId, "Unable to get collaborators: "
                    + e.getLocalizedMessage()));
            } else {
                HTLog.error(logger, e);
            }
        } catch (IOException e) {
            HTLog.error(logger, e);
        }
        return new ArrayList<>();
    }

    @Override
    public List<TurboIssue> getIssues(String repoId) {
        Map<String, String> filters = new HashMap<>();
        filters.put(IssueService.FIELD_FILTER, "all");
        filters.put(IssueService.FILTER_STATE, "all");
        return getAll(issueService.pageIssues(RepositoryId.createFromId(repoId), filters), repoId).stream()
            .map(i -> new TurboIssue(repoId, i))
            .collect(Collectors.toList());
    }

    private List<Issue> getAll(PageIterator<Issue> iterator, String repoId) {
        List<Issue> elements = new ArrayList<>();

        // Assume there is at least one page
        int knownLastPage = 1;

        try {
            while (iterator.hasNext()) {
                Collection<Issue> additions = iterator.next();
                elements.addAll(additions);

                // Compute progress

                // iterator.getLastPage() only has a value after iterator.next() is called,
                // so it's used directly in this loop. It returns the 1-based index of the last
                // page, except when we are actually on the last page, in which case it returns -1.
                // This portion deals with all these quirks.

                knownLastPage = Math.max(knownLastPage, iterator.getLastPage());
                int totalIssueCount = knownLastPage * PagedRequest.PAGE_SIZE;
                // Total is approximate: always >= the actual amount
                assert totalIssueCount >= elements.size();

                float progress = ((float) elements.size() / (float) totalIssueCount);
                UI.events.triggerEvent(new UpdateProgressEvent(repoId, progress));
                logger.info(HTLog.format(repoId, "Loaded %d issues (%.0f%% done)",
                    elements.size(), progress * 100));
            }
            UI.events.triggerEvent(new UpdateProgressEvent(repoId));
        } catch (NoSuchPageException pageException) {
            try {
                throw pageException.getCause();
            } catch (IOException e) {
                HTLog.error(logger, e);
            }
        }
        return elements;
    }

    public List<TurboIssueEvent> getEvents(String repoId, int issueId) {
        try {
            return issueService.getIssueEvents(RepositoryId.createFromId(repoId), issueId)
                .getTurboIssueEvents();
        } catch (IOException e) {
            HTLog.error(logger, e);
            return new ArrayList<>();
        }
    }

    public List<Comment> getComments(String repoId, int issueId) {
        try {
            return issueService.getComments(RepositoryId.createFromId(repoId), issueId);
        } catch (IOException e) {
            HTLog.error(logger, e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean isRepositoryValid(String repoId) {
        String repoURL = SEGMENT_REPOS + "/" + repoId;
        try {
            GitHubRequest req = new GitHubRequest();
            client.get(req.setUri(repoURL));
            return true;
        } catch (RequestException e) {
            if (e.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                return false;
            } else {
                HTLog.error(logger, e);
            }
        } catch (IOException e) {
            HTLog.error(logger, e);
        }
        return false;
    }

    @Override
    public ImmutablePair<Integer, Long> getRateLimitResetTime() throws IOException {
        return client.getRateLimitResetTime();
    }
}

