package service.updateservice;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_MILESTONES;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.client.GitHubRequest;

import service.GitHubClientExtended;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MilestoneUpdateService extends UpdateService<Milestone>{
	
	
	public MilestoneUpdateService(GitHubClientExtended client){
		super(client);
		apiSuffix = SEGMENT_MILESTONES;
	}
	
	private Map<String, String> createUpdatedMilestonesParams(){
		Map<String, String> params = new HashMap<String, String>();
		params.put("state", "all");
		return params;
	}
	
	@Override
	protected GitHubRequest createUpdatedRequest(IRepositoryIdProvider repoId){
		GitHubRequest request = super.createUpdatedRequest(repoId);
		request.setParams(createUpdatedMilestonesParams());
		request.setType(new TypeToken<Milestone>(){}.getType());
		request.setArrayType(new TypeToken<ArrayList<Milestone>>(){}.getType());
		return request;
	}
}
