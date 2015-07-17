package backend.resource.serialization;

import java.util.List;
import java.util.stream.Collectors;

import backend.UpdateSignature;
import backend.resource.Model;

/**
 * 100% immutable.
 */
public class SerializableModel {
    public final String repoId;
    public final UpdateSignature updateSignature;

    public final List<SerializableIssue> issues;
    public final List<SerializableLabel> labels;
    public final List<SerializableMilestone> milestones;
    public final List<SerializableUser> users;

    public SerializableModel(Model model) {
        this.repoId = model.getRepoId();
        this.updateSignature = model.getUpdateSignature();

        this.issues = model.getIssues().stream()
            .map(SerializableIssue::new).collect(Collectors.toList());
        this.labels = model.getLabels().stream()
            .map(SerializableLabel::new).collect(Collectors.toList());
        this.milestones = model.getMilestones().stream()
            .map(SerializableMilestone::new).collect(Collectors.toList());
        this.users = model.getUsers().stream()
            .map(SerializableUser::new).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        String formatter = "Model: {%n"
                + "repoId: %s%n"
                + "updateSignature: %s%n"
                + "users:%n%s,%n"
                + "labels:%n%s,%n"
                + "milestones:%n%s,%n"
                + "issues:%n%s%n"
                + "}";

        return String.format(
                formatter,
                repoId, updateSignature,
                users, labels, milestones, issues);
    }
}


