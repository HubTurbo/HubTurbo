package backend.stub;

import backend.github.CheckRateLimitTask;
import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;

public class CheckRateLimitTaskStub extends CheckRateLimitTask {

    public CheckRateLimitTaskStub(TaskRunner taskRunner, Repo repo) {
        super(taskRunner, repo);
    }
}
