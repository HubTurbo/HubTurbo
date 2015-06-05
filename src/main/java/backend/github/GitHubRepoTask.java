package backend.github;

import java.util.Date;
import java.util.List;

import backend.interfaces.Repo;
import backend.interfaces.RepoTask;
import backend.interfaces.TaskRunner;

public abstract class GitHubRepoTask<R> extends RepoTask<R> {

    public GitHubRepoTask(TaskRunner taskRunner, Repo repo) {
        super(taskRunner, repo);
    }

    public static class Result<TR> {
        public final List<TR> items;
        public final String eTag;
        public final Date lastCheckTime;

        public Result(List<TR> items, String eTag, Date lastCheckTime) {
            this.items = items;
            this.eTag = eTag;
            this.lastCheckTime = lastCheckTime;
        }

        public Result(List<TR> items, String eTag) {
            this.items = items;
            this.eTag = eTag;
            this.lastCheckTime = null;
        }
    }
}
