package backend.stub;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;

public class CheckRateLimitTask extends backend.github.CheckRateLimitTask {

    public CheckRateLimitTask(TaskRunner taskRunner, Repo repo) {
        super(taskRunner, repo);
    }
}
