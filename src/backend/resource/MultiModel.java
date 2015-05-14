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
}

