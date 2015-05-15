package backend.stub;

import backend.UserCredentials;
import backend.interfaces.Repo;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DummyRepo implements Repo<Issue, Label, Milestone, User> {

	private static int counter = 10;

	private static Issue updateRandomIssue() {
		int i = (int) (Math.random() * counter);
		Issue issue = new Issue();
		issue.setNumber(i);
		issue.setTitle("Issue " + i + " " + Math.random());
		return issue;
	}

	private static Issue makeDummyIssue() {
		Issue issue = new Issue();
		issue.setNumber(counter + 1);
		issue.setTitle("Issue " + (counter + 1));
		counter++;
		return issue;
	}

	public DummyRepo() {
	}

	@Override
	public boolean login(UserCredentials credentials) {
		return true;
	}

	@Override
	public ImmutableTriple<List<Issue>, String, Date> getUpdatedIssues(String repoId, String ETag, Date lastCheckTime) {
		List<Issue> issues = new ArrayList<>();
		issues.add(updateRandomIssue());
		issues.add(makeDummyIssue());
		return new ImmutableTriple<>(issues, ETag, lastCheckTime);
	}

	@Override
	public ImmutablePair<List<Label>, String> getUpdatedLabels(String repoId, String ETag) {
		return null;
	}

	@Override
	public ImmutablePair<List<Milestone>, String> getUpdatedMilestones(String repoId, String ETag) {
		return null;
	}

	@Override
	public ImmutablePair<List<User>, String> getUpdatedUsers(String repoId, String ETag) {
		return null;
	}

	@Override
	public List<Issue> getIssues(String repoName) {
		List<Issue> issues = new ArrayList<>();
		for (int i=0; i<10; i++) {
			issues.add(makeDummyIssue());
		}
		return issues;
	}

	@Override
	public List<Label> getLabels(String repoId) {
		return null;
	}

	@Override
	public List<Milestone> getMilestones(String repoId) {
		return null;
	}

	@Override
	public List<User> getUsers(String repoId) {
		return null;
	}
}
