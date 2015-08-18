package backend.stub;

import backend.github.UpdateModelTask;
import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;

public class UpdateModelTaskStub extends UpdateModelTask {

    public UpdateModelTaskStub(TaskRunner taskRunner, Repo repo, Model model) {
        super(taskRunner, repo, model);
    }
}
