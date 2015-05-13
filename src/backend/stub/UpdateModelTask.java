package backend.stub;

import backend.Model;
import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import org.eclipse.egit.github.core.Issue;

public class UpdateModelTask extends backend.github.UpdateModelTask {

	public UpdateModelTask(TaskRunner taskRunner, Repo<Issue> repo, Model model) {
		super(taskRunner, repo, model);
	}
}
