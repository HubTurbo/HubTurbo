package backend.interfaces;

import backend.UserCredentials;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.eclipse.egit.github.core.Issue;

import java.util.Date;
import java.util.List;

// This is not completely generic as it relies on eGit's Issue class.
// An ideal implementation would probably rely on our own back-end-agnostic
// representation of raw issue data. // This could be achieved by adding
// generic parameters to all related classes to replace concrete Issues,
// Labels, etc.

public interface Repo {
	public List<Issue> getIssues(String repoId);
	public boolean login(UserCredentials credentials);

	// Returns a tuple in order to be maximally generic
	public ImmutableTriple<List<Issue>, String, Date> getUpdatedIssues(String repoId, String ETag, Date lastCheckTime);

}
