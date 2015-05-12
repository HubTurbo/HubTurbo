package backend;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 100% immutable
 */
public class SerializableModel {
	public final List<SerializableIssue> issues;
	public final String repoId;
	public final UpdateSignature updateSignature;

	public SerializableModel(Model model) {
		this.repoId = model.getRepoId().generateId();
		this.issues = model.getIssues().stream()
			.map(SerializableIssue::new).collect(Collectors.toList());
		this.updateSignature = model.getUpdateSignature();
	}

	public SerializableModel(String repoName, UpdateSignature updateSignature, List<SerializableIssue> issues) {
		this.repoId = repoName;
		this.updateSignature = updateSignature;
		this.issues = issues;
	}
}


