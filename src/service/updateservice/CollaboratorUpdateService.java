package service.updateservice;


import java.util.ArrayList;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.PagedRequest;

import service.GitHubClientExtended;

import com.google.gson.reflect.TypeToken;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COLLABORATORS;

public class CollaboratorUpdateService extends UpdateService<User>{	

	public CollaboratorUpdateService(GitHubClientExtended client){
		super(client);
		apiSuffix = SEGMENT_COLLABORATORS;
	}
	
	@Override
	protected PagedRequest<User> createUpdatedRequest(IRepositoryIdProvider repoId){
		PagedRequest<User> request = super.createUpdatedRequest(repoId);
		request.setType(new TypeToken<User>(){}.getType());
		request.setArrayType(new TypeToken<ArrayList<User>>(){}.getType());
		return request;
	}
}
