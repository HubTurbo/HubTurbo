package backend.stub;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;

public class UpdateModelTask extends backend.github.UpdateModelTask {

	public UpdateModelTask(TaskRunner taskRunner, Repo repo, Model model) {
		super(taskRunner, repo, model);
	}
}
