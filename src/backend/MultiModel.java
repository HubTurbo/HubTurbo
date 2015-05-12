package backend;

import backend.interfaces.IModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// TODO may need synchronisation on add
public class MultiModel implements IModel {

	private final HashMap<String, IModel> models;

	public MultiModel() {
		this.models = new HashMap<>();
	}

	public void add(Model model) {
		models.put(model.getRepoId().generateId(), model);
	}

	public IModel get(String repoId) {
		return models.get(repoId);
	}

	@Override
	public List<TurboIssue> getIssues() {
		List<TurboIssue> result = new ArrayList<>();
		models.values().forEach(m -> result.addAll(m.getIssues()));
		return result;
	}
}
