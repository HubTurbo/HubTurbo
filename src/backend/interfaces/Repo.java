package backend.interfaces;

import backend.UserCredentials;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.Date;
import java.util.List;

public interface Repo<I> {
	public List<I> getIssues(String repoId);
	public boolean login(UserCredentials credentials);

	// Returns a tuple in order to be maximally generic
	public ImmutableTriple<List<I>, String, Date> getUpdatedIssues(String repoId, String ETag, Date lastCheckTime);

}
