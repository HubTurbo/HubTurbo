package backend.resource;

import backend.UpdateSignature;
import backend.interfaces.IModel;
import backend.resource.serialization.SerializableModel;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryId;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Model implements IModel {

	private final UpdateSignature updateSignature;
	private final IRepositoryIdProvider repoId;
	private final List<TurboIssue> issues;
	private final List<TurboLabel> labels;
	private final List<TurboMilestone> milestones;
	private final List<TurboUser> users;

	/**
	 * Standard constructor
	 */
	public Model(IRepositoryIdProvider repoId, List<TurboIssue> issues,
		List<TurboLabel> labels, List<TurboMilestone> milestones, List<TurboUser> users,
		UpdateSignature updateSignature) {

		this.updateSignature = updateSignature;
		this.repoId = repoId;
		this.issues = issues;
		this.labels = labels;
		this.milestones = milestones;
		this.users = users;
	}

	/**
	 * Standard constructor with empty update signature -- for use when
	 * a model is first downloaded
	 */
	public Model(IRepositoryIdProvider repoId, List<TurboIssue> issues,
		List<TurboLabel> labels, List<TurboMilestone> milestones, List<TurboUser> users) {

		this.updateSignature = UpdateSignature.empty;
		this.repoId = repoId;
		this.issues = issues;
		this.labels = labels;
		this.milestones = milestones;
		this.users = users;
	}

	/**
	 * Constructor for the empty model
	 */
	public Model(IRepositoryIdProvider repoId, UpdateSignature updateSignature) {
		this.updateSignature = updateSignature;
		this.repoId = repoId;
		this.issues = new ArrayList<>();
		this.labels = new ArrayList<>();
		this.milestones = new ArrayList<>();
		this.users = new ArrayList<>();
	}

	/**
	 * Copy constructor
	 */
	public Model(Model model) {
		this.updateSignature = model.updateSignature;
		this.repoId = model.getRepoId();
		this.issues = new ArrayList<>(model.getIssues());
		this.labels = new ArrayList<>(model.getLabels());
		this.milestones = new ArrayList<>(model.getMilestones());
		this.users = new ArrayList<>(model.getUsers());
	}

	public Model(SerializableModel model) {
		this.updateSignature = model.updateSignature;
		this.repoId = RepositoryId.createFromId(model.repoId);
		this.issues = model.issues.stream()
			.map(TurboIssue::new)
			.collect(Collectors.toList());
		this.labels = model.labels.stream()
			.map(TurboLabel::new)
			.collect(Collectors.toList());
		this.milestones = model.milestones.stream()
			.map(TurboMilestone::new)
			.collect(Collectors.toList());
		this.users = model.users.stream()
			.map(TurboUser::new)
			.collect(Collectors.toList());
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
		return new ArrayList<>(issues);
	}

	@Override
	public List<TurboLabel> getLabels() {
		return new ArrayList<>(labels);
	}

	@Override
	public List<TurboMilestone> getMilestones() {
		return new ArrayList<>(milestones);
	}

	@Override
	public List<TurboUser> getUsers() {
		return new ArrayList<>(users);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Model model = (Model) o;

		if (!issues.equals(model.issues)) return false;
		if (!labels.equals(model.labels)) return false;
		if (!milestones.equals(model.milestones)) return false;
		if (!repoId.equals(model.repoId)) return false;
		if (!updateSignature.equals(model.updateSignature)) return false;
		if (!users.equals(model.users)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = updateSignature.hashCode();
		result = 31 * result + repoId.hashCode();
		result = 31 * result + issues.hashCode();
		result = 31 * result + labels.hashCode();
		result = 31 * result + milestones.hashCode();
		result = 31 * result + users.hashCode();
		return result;
	}
}
