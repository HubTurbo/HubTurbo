package backend.resource;

import backend.IssueMetadata;
import backend.interfaces.IModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import prefs.Preferences;
import util.Utility;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This represents the true state of local repositories data. Operations meant to change the state
 * of local repositories data but do not go through the methods in this class i.e. operating on
 * dangling references its sub-components are considered unsafe
 * Thread-safe. The only top-level state in the application.
 */
@SuppressWarnings("unused")
public class MultiModel implements IModel {

    private final HashMap<String, Model> models;
    private final Preferences prefs;

    // A pending repository is one that has been requested to load but has
    // not finished loading. We keep track of it because we don't want repeated
    // requests for the same repository to load it multiple times.
    private final HashSet<String> pendingRepositories;

    // Guaranteed to have a value throughout
    private String defaultRepo = null;

    private static final Logger logger = LogManager.getLogger(MultiModel.class.getName());

    public MultiModel(Preferences prefs) {
        this.models = new HashMap<>();
        this.pendingRepositories = new HashSet<>();
        this.prefs = prefs;
    }

    public synchronized MultiModel addPending(Model model) {
        String repoId = model.getRepoId();
        Optional<String> matchingRepoId = pendingRepositories.stream()
                .filter(pendingRepo -> pendingRepo.equalsIgnoreCase
                        (repoId))
                .findFirst();
        assert matchingRepoId.isPresent() : "No pending repository " + repoId + "!";
        pendingRepositories.remove(matchingRepoId.get());
        add(model);
        preprocessNewIssues(model);
        return this;
    }

    private synchronized MultiModel add(Model model) {
        this.models.put(model.getRepoId(), model);
        return this;
    }

    public synchronized MultiModel removeRepoModelById(String repoId) {
        Optional<String> repoIdCorrectCase = models.keySet().stream()
                .filter(key -> key.equalsIgnoreCase(repoId)).findFirst();
        if (!repoIdCorrectCase.isPresent()) {
            logger.error("RepoId specified does not have a model.");
        }

        Optional<Model> repoModelToBeDeleted = getModelById(repoIdCorrectCase.get());
        if (repoModelToBeDeleted.isPresent()) {
            this.models.remove(repoModelToBeDeleted.get().getRepoId());
        } else {
            logger.error("RepoModel to be deleted does not exist.");
        }
        return this;
    }

    public synchronized Model get(String repoId) {
        return models.get(repoId);
    }

    public synchronized List<Model> toModels() {
        return new ArrayList<>(models.values());
    }

    public synchronized MultiModel replace(List<Model> newModels) {
        this.models.clear();
        newModels.forEach(this::add);
        return this;
    }

    public synchronized MultiModel replace(Model newModel) {
        this.add(newModel);
        return this;
    }

    /**
     * Replaces labels of an issue specified by {@code issueId} in {@code repoId} with {@code labels}
     *
     * @param repoId
     * @param issueId
     * @param labels
     * @return the modified TurboIssue if successful
     */
    public synchronized Optional<TurboIssue> replaceIssueLabels(String repoId, int issueId, List<String> labels) {
        Optional<Model> modelLookUpResult = getModelById(repoId);
        return Utility.safeFlatMapOptional(modelLookUpResult, (model) -> {
            return model.replaceIssueLabels(issueId, labels);
        }, () -> logger.error("Model " + repoId + " not found in models"));
    }

    /**
     * Replaces the milestone of an issue specified by {@code issueId} in {@code repoId} with {@code milestone}
     *
     * @param repoId
     * @param issueId
     * @param milestone
     * @return the modified TurboIssue if successful
     */
    public synchronized Optional<TurboIssue> replaceIssueMilestone(String repoId, int issueId,
                                                                   Optional<Integer> milestone) {
        Optional<Model> modelLookUpResult = getModelById(repoId);
        return Utility.safeFlatMapOptional(modelLookUpResult, (model) -> {
            return model.replaceIssueMilestone(issueId, milestone);
        }, () -> logger.error("Model " + repoId + " not found in models"));
    }

    /**
     * Sets the open/closed state of an issue specified by {@code issueId} in {@code repoId} to {@code isOpen}
     *
     * @param repoId
     * @param issueId
     * @param isOpen
     * @return the modified TurboIssue if successful
     */
    public synchronized Optional<TurboIssue> editIssueState(String repoId, int issueId, boolean isOpen) {
        Optional<Model> modelLookUpResult = getModelById(repoId);
        return Utility.safeFlatMapOptional(modelLookUpResult,
            (model) -> model.editIssueState(issueId, isOpen),
            () -> logger.error("Model " + repoId + " not found in models"));
    }

    public synchronized void insertMetadata(String repoId, Map<Integer, IssueMetadata> metadata, String currentUser) {
        models.get(repoId).getIssues().forEach(issue -> {
            if (metadata.containsKey(issue.getId())) {
                IssueMetadata toBeInserted = metadata.get(issue.getId());

                // ETag comparison is based on IssueMetadata constructor for more granularity, so that we can choose
                // to not replace events while still replacing comments in the case of same ETag.
                // TODO move ETag comparison here when comments ETag implementation is complete.
                LocalDateTime nonSelfUpdatedAt = reconcileCreationDate(toBeInserted.getNonSelfUpdatedAt(),
                                                                       issue.getCreatedAt(), currentUser, issue
                                                                               .getCreator());
                issue.setMetadata(toBeInserted.reconcile(nonSelfUpdatedAt,
                                                         issue.getMetadata().getEvents(), issue.getMetadata()
                                                                 .getEventsETag()));
            }
        });
    }

    private static LocalDateTime reconcileCreationDate(LocalDateTime lastNonSelfUpdate,
                                                       LocalDateTime creationTime,
                                                       String currentUser,
                                                       String issueCreator) {
        // If current user is same as issue creator, we do not consider creation of issue
        // as an issue update.
        if (currentUser.equalsIgnoreCase(issueCreator)) return lastNonSelfUpdate;

        // Current user is not the issue creator.
        // lastNonSelfUpdate cannot be before creationTime unless it is the default value (epoch 0),
        // which means the issue has no events or comments.
        // However, since the current user is not the issue creator, creation of the issue itself
        // counts as an update.
        if (lastNonSelfUpdate.compareTo(creationTime) < 0) return creationTime;

        // Otherwise, the issue actually possesses non-self updates, so we do nothing with the
        // value.
        return lastNonSelfUpdate;
    }

    @Override
    public synchronized String getDefaultRepo() {
        return defaultRepo;
    }

    @Override
    public synchronized void setDefaultRepo(String repoId) {
        this.defaultRepo = repoId;
    }

    @Override
    public boolean isUserInRepo(String repoId, String userName) {
        List<TurboUser> usersOfRepo = getUsersOfRepo(repoId);
        return usersOfRepo.stream()
                .filter(userOfRepo -> userOfRepo.getRealName().toLowerCase().contains(userName.toLowerCase()) ||
                        userOfRepo.getLoginName().toLowerCase().contains(userName.toLowerCase()))
                .findFirst()
                .isPresent();
    }

    @Override
    public synchronized List<TurboIssue> getIssues() {
        List<TurboIssue> result = new ArrayList<>();
        models.values().forEach(m -> result.addAll(m.getIssues()));
        return result;
    }

    @Override
    public synchronized List<TurboLabel> getLabels() {
        List<TurboLabel> result = new ArrayList<>();
        models.values().forEach(m -> result.addAll(m.getLabels()));
        return result;
    }

    @Override
    public synchronized List<TurboMilestone> getMilestones() {
        List<TurboMilestone> result = new ArrayList<>();
        models.values().forEach(m -> result.addAll(m.getMilestones()));
        return result;
    }

    @Override
    public synchronized List<TurboUser> getUsers() {
        List<TurboUser> result = new ArrayList<>();
        models.values().forEach(m -> result.addAll(m.getUsers()));
        return result;
    }

    @Override
    public synchronized Optional<Model> getModelById(String repoId) {
        return models.containsKey(repoId)
                ? Optional.of(models.get(repoId))
                : Optional.empty();
    }

    @Override
    public Optional<TurboUser> getAssigneeOfIssue(TurboIssue issue) {
        return getModelById(issue.getRepoId())
                .flatMap(m -> m.getAssigneeOfIssue(issue));
    }

    @Override
    public Optional<TurboUser> getAuthorOfIssue(TurboIssue issue) {
        return getModelById(issue.getRepoId())
                .flatMap(m -> m.getCreatorOfIssue(issue));
    }

    @Override
    public List<TurboLabel> getLabelsOfIssue(TurboIssue issue, Predicate<TurboLabel> predicate) {
        return getModelById(issue.getRepoId())
                .flatMap(m -> Optional.of(m.getLabelsOfIssue(issue)))
                .get().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public List<TurboUser> getUsersOfRepo(String repoId) {
        Optional<Model> model = getModelById(repoId);
        return model.isPresent() ? model.get().getUsers() : new ArrayList<>();
    }

    @Override
    public List<TurboLabel> getLabelsOfIssue(TurboIssue issue) {
        return getModelById(issue.getRepoId())
                .flatMap(m -> Optional.of(m.getLabelsOfIssue(issue)))
                .get();
    }

    @Override
    public Optional<TurboMilestone> getMilestoneOfIssue(TurboIssue issue) {
        return getModelById(issue.getRepoId())
                .flatMap(m -> m.getMilestoneOfIssue(issue));
    }

    public synchronized boolean isRepositoryPending(String repoId) {
        return pendingRepositories.stream().anyMatch(pendingRepo -> pendingRepo.equalsIgnoreCase(repoId));
    }

    public void queuePendingRepository(String repoId) {
        pendingRepositories.add(repoId);
    }

    /**
     * Called on new models which come in.
     * Mutates TurboIssues with meta-information.
     *
     * @param model
     */
    private void preprocessNewIssues(Model model) {
        // All new issues which come in are not read, unless they already were according to prefs.
        for (TurboIssue issue : model.getIssues()) {
            Optional<LocalDateTime> time = prefs.getMarkedReadAt(model.getRepoId(), issue.getId());
            issue.setMarkedReadAt(time);
        }
    }

    @SuppressWarnings("unused")
    private void ______BOILERPLATE______() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiModel that = (MultiModel) o;
        return models.equals(that.models);
    }

    @Override
    public int hashCode() {
        return models.hashCode();
    }

}

