package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.RepoTask;
import backend.interfaces.TaskRunner;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;

import java.util.Date;
import java.util.List;

public abstract class GitHubRepoTask<R> extends RepoTask<R, Issue, Label, Milestone, User> {

	public GitHubRepoTask(TaskRunner taskRunner, Repo<Issue, Label, Milestone, User> repo) {
		super(taskRunner, repo);
	}

	public static class Result<TR> {
		public final List<TR> items;
		public final String ETag;
		public final Date lastCheckTime;

		public Result(List<TR> items, String eTag, Date lastCheckTime) {
			this.items = items;
			this.ETag = eTag;
			this.lastCheckTime = lastCheckTime;
		}

		public Result(List<TR> items, String eTag) {
			this.items = items;
			this.ETag = eTag;
			this.lastCheckTime = null;
		}
	}
}
