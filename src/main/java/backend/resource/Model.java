package backend.resource;

import backend.UpdateSignature;
import backend.interfaces.IBaseModel;
import backend.resource.serialization.SerializableModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Model implements IBaseModel {

	private final UpdateSignature updateSignature;
	private final String repoId;
	private final List<TurboIssue> issues;
	private final List<TurboLabel> labels;
	private final List<TurboMilestone> milestones;
	private final List<TurboUser> users;

	/**
	 * Standard constructor
	 */
	public Model(String repoId, List<TurboIssue> issues,
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
	public Model(String repoId, List<TurboIssue> issues,
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
	public Model(String repoId) {
		this.updateSignature = UpdateSignature.empty;
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
		this.repoId = model.repoId;
		this.issues = model.issues.stream()
			.map(i -> new TurboIssue(model.repoId, i))
			.collect(Collectors.toList());
		this.labels = model.labels.stream()
			.map(l -> new TurboLabel(model.repoId, l))
			.collect(Collectors.toList());
		this.milestones = model.milestones.stream()
			.map(m -> new TurboMilestone(model.repoId, m))
			.collect(Collectors.toList());
		this.users = model.users.stream()
			.map(u -> new TurboUser(model.repoId, u))
			.collect(Collectors.toList());
	}

	public String getRepoId() {
		return repoId;
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

	private void ______OPERATIONS_____() {
	}

	public Optional<TurboIssue> getIssueById(int issueId) {
		assert issueId >= 1 : "Invalid issue id " + issueId;
		for (TurboIssue issue : getIssues()) {
			if (issue.getId() == issueId) {
				return Optional.of(issue);
			}
		}
		return Optional.empty();
	}

	public Optional<TurboLabel> getLabelByActualName(String labelName) {
		assert labelName != null && !labelName.isEmpty() : "Invalid label name " + labelName;
		for (TurboLabel label : getLabels()) {
			if (label.getActualName().equals(labelName)) {
				return Optional.of(label);
			}
		}
		return Optional.empty();
	}

	public Optional<TurboUser> getUserByLogin(String login) {
		assert login != null && !login.isEmpty() : "Invalid user name " + login;
		for (TurboUser user : getUsers()) {
			if (user.getLoginName().equals(login)) {
				return Optional.of(user);
			}
		}
		return Optional.empty();
	}

	public Optional<TurboMilestone> getMilestoneByTitle(String title) {
		assert title != null && !title.isEmpty() : "Invalid milestone title " + title;
		for (TurboMilestone milestone : getMilestones()) {
			if (milestone.getTitle().equals(title)) {
				return Optional.of(milestone);
			}
		}
		return Optional.empty();
	}

	public Optional<TurboMilestone> getMilestoneById(int id) {
		assert id >= 1 : "Invalid milestone id " + id;
		for (TurboMilestone milestone : getMilestones()) {
			if (milestone.getId() == id) {
				return Optional.of(milestone);
			}
		}
		return Optional.empty();
	}

	public Optional<TurboMilestone> getMilestoneOfIssue(TurboIssue issue) {
		return issue.getMilestone().flatMap(this::getMilestoneById);
	}

	public Optional<TurboUser> getAssigneeOfIssue(TurboIssue issue) {
		return issue.getAssignee().flatMap(this::getUserByLogin);
	}

	public List<TurboLabel> getLabelsOfIssue(TurboIssue issue) {
		return issue.getLabels().stream()
			.map(this::getLabelByActualName)
			.filter(Optional::isPresent).map(Optional::get)
			.collect(Collectors.toList());
	}

	private void ______BOILERPLATE______() {
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
