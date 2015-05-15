package backend.stub;

import backend.resource.Model;
import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;

public class UpdateModelTask extends backend.github.UpdateModelTask {

	public UpdateModelTask(TaskRunner taskRunner, Repo<Issue, Label, Milestone, User> repo, Model model) {
		super(taskRunner, repo, model);
	}
}
