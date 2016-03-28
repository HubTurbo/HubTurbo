package backend.github;

import backend.UserCredentials;
import backend.interfaces.Repo;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import github.*;
import github.update.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.*;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.MilestoneService;
import ui.UI;
import util.HTLog;
import util.events.UpdateProgressEvent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

public class GitHubRepo implements Repo {

    private static final Logger logger = HTLog.get(GitHubRepo.class);

    private final GitHubClientEx client = new GitHubClientEx();
    private final IssueServiceEx issueService = new IssueServiceEx(client);
    private final PullRequestServiceEx pullRequestService = new PullRequestServiceEx(client);
    private final CollaboratorServiceEx collaboratorService = new CollaboratorServiceEx(client);
    private final LabelServiceEx labelService = new LabelServiceEx(client);
    private final MilestoneService milestoneService = new MilestoneService(client);

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
        return new ImmutableTriple<>(items, issueUpdateService.getUpdatedETags(),
                                     issueUpdateService.getUpdatedCheckTime());
    }

    @Override
    public List<PullRequest> getUpdatedPullRequests(String repoId, Date lastCheckTime) {
        PullRequestUpdateService updateService = new PullRequestUpdateService(client, lastCheckTime);
        return updateService.getUpdatedItems(RepositoryId.createFromId(repoId));
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
            String repoId, String eTag, BiFunction<GitHubClientEx, String, S> constructService,
            BiFunction<String, R, TR> resourceConstructor) {

        S updateService = constructService.apply(client, eTag);
        List<R> updatedItems = updateService.getUpdatedItems(RepositoryId.createFromId(repoId));
        List<TR> items = updatedItems.stream()
                .map(i -> resourceConstructor.apply(repoId, i))
                .collect(Collectors.toList());
        return new ImmutablePair<>(items,
                                   updateService.getUpdatedETags());
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

                float progress = (float) elements.size() / (float) totalIssueCount;
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

    @Override
    public ImmutablePair<List<TurboIssueEvent>, String> getUpdatedEvents(String repoId,
                                                                         int issueId,
                                                                         String currentETag) {
        try {
            GitHubEventsResponse eventsResponse = issueService.getIssueEvents(
                    RepositoryId.createFromId(repoId), issueId, currentETag);
            return new ImmutablePair<>(eventsResponse.getTurboIssueEvents(), eventsResponse.getUpdatedETag());
        } catch (IOException e) {
            HTLog.error(logger, e);
            return new ImmutablePair<>(new ArrayList<>(), currentETag);
        }
    }

    @Override
    public List<Comment> getComments(String repoId, int issueId) {
        try {
            return issueService.getComments(RepositoryId.createFromId(repoId), issueId);
        } catch (IOException e) {
            HTLog.error(logger, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ReviewComment> getReviewComments(String repoId, int pullRequestId) {
        try {
            return pullRequestService.getReviewComments(RepositoryId.createFromId(repoId),
                                                        pullRequestId);
        } catch (IOException e) {
            HTLog.error(logger, e);
            return new ArrayList<>();
        }
    }

    /**
     * Get all types of comments for an issue. Review comments and commit comments
     * are only relevant if the issue is a also pull request
     *
     * @param repoId
     * @param issue
     * @return list of comments for an issue
     */
    @Override
    public List<Comment> getAllComments(String repoId, TurboIssue issue) {
        List<Comment> result = new ArrayList<>();

        result.addAll(getComments(repoId, issue.getId()));
        if (issue.isPullRequest()) {
            result.addAll(getReviewComments(repoId, issue.getId()));
        }

        return result;
    }

    @Override
    public List<Label> setLabels(String repoId, int issueId, List<String> labels) throws IOException {
        return labelService.setLabels(
                RepositoryId.createFromId(repoId),
                String.valueOf(issueId),
                labels.stream()
                        .map(labelName -> new Label().setName(labelName))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<Integer> setMilestone(String repoId, int issueId, String issueTitle,
                                          Optional<Integer> issueMilestone) throws IOException {
        // github api requires at least id and title
        Issue createdIssue = new Issue();
        createdIssue.setNumber(issueId);
        createdIssue.setTitle(issueTitle);

        Milestone gitHubMilestone = new Milestone();
        // set milestone number to the desired milestone id
        // simply don't set a number to demilestone
        issueMilestone.ifPresent(gitHubMilestone::setNumber);
        createdIssue.setMilestone(gitHubMilestone);

        Issue returnedIssue = issueService.editIssue(RepositoryId.createFromId(repoId), createdIssue);

        return Optional.ofNullable(returnedIssue.getMilestone())
                .map(Milestone::getNumber);
    }

    /**
     * Calls GitHub to change the open/close state of an issue.
     *
     * @param repoId
     * @param issueId
     * @param isOpen
     * @return {@code true} if success, {@code false} otherwise
     * @throws IOException
     */
    public boolean editIssueState(String repoId, int issueId, boolean isOpen) throws IOException {
        Issue updatedIssue = issueService.editIssueState(
                RepositoryId.createFromId(repoId),
                issueId,
                isOpen
        );

        return updatedIssue.getState().equals(isOpen ? IssueService.STATE_OPEN : IssueService.STATE_CLOSED);
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

