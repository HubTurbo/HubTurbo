package backend.resource;

import backend.IssueMetadata;
import backend.interfaces.IModel;

import java.util.*;

/**
 * Thread-safe. The only top-level state in the application.
 */
@SuppressWarnings("unused")
public class MultiModel implements IModel {

	private final HashMap<String, Model> models;

	// A pending repository is one that has been requested to load but has
	// not finished loading. We keep track of it because we don't want repeated
	// requests for the same repository to load it multiple times.
	private final HashSet<String> pendingRepositories;

	// Guaranteed to have a value throughout
	private String defaultRepo = null;

	public MultiModel() {
		this.models = new HashMap<>();
		this.pendingRepositories = new HashSet<>();
	}

	public synchronized MultiModel addPending(Model model) {
		String repoId = model.getRepoId();
		assert pendingRepositories.contains(repoId) : "No pending repository " + repoId + "!";
		pendingRepositories.remove(repoId);
		add(model);
		return this;
	}

	public synchronized MultiModel add(Model model) {
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
		this.models.clear();
		newModels.forEach(this::add);
		return this;
	}

	public synchronized void insertMetadata(String repoId, Map<Integer, IssueMetadata> metadata) {
		models.get(repoId).getIssues().forEach(issue -> {
			if (metadata.containsKey(issue.getId())) {
				issue.setMetadata(new IssueMetadata(metadata.get(issue.getId())));
			}
		});
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
		return pendingRepositories.contains(repoId);
	}

	public void addPendingRepository(String repoId) {
		pendingRepositories.add(repoId);
	}

	private void ______BOILERPLATE______() {
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MultiModel that = (MultiModel) o;

		if (!models.equals(that.models)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return models.hashCode();
	}

}

