package github.update;


import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COLLABORATORS;

import java.util.ArrayList;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PagedRequest;

import com.google.gson.reflect.TypeToken;

import github.GitHubClientExtended;

public class UserUpdateService extends UpdateService<User>{

	public UserUpdateService(GitHubClientExtended client, String collabsETag){
		super(client, SEGMENT_COLLABORATORS, collabsETag);
	}
	
	@Override
	protected PagedRequest<User> createUpdatedRequest(IRepositoryIdProvider repoId){
		PagedRequest<User> request = super.createUpdatedRequest(repoId);
		request.setType(new TypeToken<User>(){}.getType());
		request.setArrayType(new TypeToken<ArrayList<User>>(){}.getType());
		return request;
	}
}
