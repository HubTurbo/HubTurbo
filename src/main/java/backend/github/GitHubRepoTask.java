package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.RepoTask;
import backend.interfaces.TaskRunner;

import java.util.Date;
import java.util.List;

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
            this.lastCheckTime = new Date(lastCheckTime.getTime());
        }

        public Result(List<TR> items, String eTag) {
            this.items = items;
            this.eTag = eTag;
            this.lastCheckTime = null;
        }
    }
}
