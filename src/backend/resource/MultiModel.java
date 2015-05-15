package backend.resource;

import backend.interfaces.IModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Thread-safe. The only top-level state in the application.
 */
public class MultiModel implements IModel {

	private final HashMap<String, Model> models;

	public MultiModel() {
		this.models = new HashMap<>();
	}

	public synchronized void add(Model model) {
		models.put(model.getRepoId().generateId(), model);
	}

	public synchronized Model get(String repoId) {
		return models.get(repoId);
	}

	public synchronized List<Model> toModels() {
		return new ArrayList<>(models.values());
	}

	public synchronized void replace(List<Model> models) {
		this.models.clear();
		models.forEach(this::add);
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

