package backend.resource;

import backend.IssueMetadata;
import backend.interfaces.IModel;
import prefs.Preferences;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
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

    public MultiModel(Preferences prefs) {
        this.models = new HashMap<>();
        this.pendingRepositories = new HashSet<>();
        this.prefs = prefs;
    }

    public synchronized MultiModel addPending(Model model) {
        String repoId = model.getRepoId();
        Optional<String> matchingRepoId = pendingRepositories.stream()
                .filter(pendingRepo -> pendingRepo.equalsIgnoreCase(repoId))
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

    public synchronized Model get(String repoId) {
        return models.get(repoId);
    }

    public synchronized List<Model> toModels() {
        return new ArrayList<>(models.values());
    }

    public synchronized MultiModel replace(List<Model> newModels) {
        preprocessUpdatedIssues(newModels);
        this.models.clear();
        newModels.forEach(this::add);
        return this;
    }

    public synchronized void insertMetadata(String repoId, Map<Integer, IssueMetadata> metadata, String currentUser) {
        models.get(repoId).getIssues().forEach(issue -> {
            if (metadata.containsKey(issue.getId())) {
                IssueMetadata toBeInserted = metadata.get(issue.getId());
                LocalDateTime nonSelfUpdatedAt = reconcileCreationDate(toBeInserted.getNonSelfUpdatedAt(),
                        issue.getCreatedAt(), currentUser, issue.getCreator());
                issue.setMetadata(new IssueMetadata(toBeInserted, nonSelfUpdatedAt));
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
    public Optional<Model> getModelById(String repoId) {
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
    public List<TurboLabel> getLabelsOfIssue(TurboIssue issue, Predicate<TurboLabel> predicate) {
        return getModelById(issue.getRepoId())
            .flatMap(m -> Optional.of(m.getLabelsOfIssue(issue)))
            .get().stream()
            .filter(predicate)
            .collect(Collectors.toList());
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
     * @param model
     */
    private void preprocessNewIssues(Model model) {
        // All new issues which come in are not read, unless they already were according to prefs.
        for (TurboIssue issue : model.getIssues()) {
            Optional<LocalDateTime> time = prefs.getMarkedReadAt(model.getRepoId(), issue.getId());
            issue.setMarkedReadAt(time);
            issue.setIsCurrentlyRead(time.isPresent());
        }
    }

    /**
     * Called on existing models that are updated.
     * Mutates TurboIssues with meta-information.
     * @param newModels
     */
    private void preprocessUpdatedIssues(List<Model> newModels) {
        // Updates preferences with the results of issues that have been updated after a refresh.
        // This makes read issues show up again.
        for (Model model : newModels) {
            assert models.containsKey(model.getRepoId());
            Model existingModel = models.get(model.getRepoId());
            if (!existingModel.getIssues().equals(model.getIssues())) {
                // Find issues that have changed and update preferences with them
                for (int i = 1; i <= model.getIssues().size(); i++) {
                    // TODO O(n^2), optimise by preprocessing into a map or sorting
                    if (!existingModel.getIssueById(i).equals(model.getIssueById(i))) {
                        assert model.getIssueById(i).isPresent();
                        // It's no longer currently read, but it retains its updated time.
                        // No changes to preferences.
                        model.getIssueById(i).get().setIsCurrentlyRead(false);
                    }
                }
            }
        }
    }

    private void ______BOILERPLATE______() {
    }

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

