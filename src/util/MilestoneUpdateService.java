package util;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_MILESTONES;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.client.GitHubRequest;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class MilestoneUpdateService extends UpdateService<Milestone>{
	
	
	public MilestoneUpdateService(GitHubClientExtended client){
		super(client);
		apiSuffix = SEGMENT_MILESTONES;
	}
	
	@Override
	protected GitHubRequest createUpdatedRequest(IRepositoryIdProvider repoId){
		GitHubRequest request = super.createUpdatedRequest(repoId);
		request.setType(new TypeToken<Milestone>(){}.getType());
		request.setArrayType(new TypeToken<ArrayList<Milestone>>(){}.getType());
		return request;
	}
}
