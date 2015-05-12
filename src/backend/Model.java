package backend;

import backend.interfaces.IModel;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryId;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Model implements IModel {

	private final UpdateSignature updateSignature;
	private final IRepositoryIdProvider repoId;
	private final List<TurboIssue> issues;

	/**
	 * Standard constructor
	 */
	public Model(IRepositoryIdProvider repoId, List<TurboIssue> issues, UpdateSignature updateSignature) {
		this.updateSignature = updateSignature;
		this.repoId = repoId;
		this.issues = issues;
	}

	/**
	 * Standard constructor with empty update signature -- for use when
	 * a model is first downloaded
	 */
	public Model(IRepositoryIdProvider repoId, List<TurboIssue> issues) {
		this.updateSignature = UpdateSignature.empty;
		this.repoId = repoId;
		this.issues = issues;
	}

	/**
	 * Constructor for the empty model
	 */
	public Model(IRepositoryIdProvider repoId, UpdateSignature updateSignature) {
		this.updateSignature = updateSignature;
		this.repoId = repoId;
		this.issues = new ArrayList<>();
	}

	/**
	 * Means of translating to SerializableModel
	 */
	public Model(SerializableModel model) {
		this.updateSignature = model.updateSignature;
		this.repoId = RepositoryId.createFromId(model.repoId);
		this.issues = model.issues.stream()
			.map(TurboIssue::new)
			.collect(Collectors.toList());
	}

	/**
	 * Copy constructor
	 */
	public Model(Model model) {
		this.updateSignature = model.updateSignature;
		this.repoId = model.getRepoId();
		this.issues = new ArrayList<>(model.getIssues());
	}

	/**
	 * For immutable updates
	 */
	public Model withIssues(List<TurboIssue> issues) {
		Model result = new Model(this);
		result.issues.clear();
		result.issues.addAll(issues);
		return result;
	}

	public IRepositoryIdProvider getRepoId() {
		return RepositoryId.createFromId(repoId.generateId());
	}

	public UpdateSignature getUpdateSignature() {
		return updateSignature;
	}

	@Override
	public List<TurboIssue> getIssues() {
		return issues;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Model model = (Model) o;

		if (issues != null ? !issues.equals(model.issues) : model.issues != null) return false;
		if (repoId != null ? !repoId.equals(model.repoId) : model.repoId != null) return false;
		if (updateSignature != null ? !updateSignature.equals(model.updateSignature) : model.updateSignature != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = updateSignature != null ? updateSignature.hashCode() : 0;
		result = 31 * result + (repoId != null ? repoId.hashCode() : 0);
		result = 31 * result + (issues != null ? issues.hashCode() : 0);
		return result;
	}
}
