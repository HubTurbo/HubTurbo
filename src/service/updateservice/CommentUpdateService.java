package service.updateservice;

import static org.eclipse.egit.github.core.client.IGitHubConstants.CONTENT_TYPE_JSON;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_ISSUES;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COMMENTS;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.GitHubRequest;

import com.google.gson.reflect.TypeToken;

import service.GitHubClientExtended;
import service.ServiceManager;

public class CommentUpdateService extends UpdateService<Comment>{
	
	private int issueId;
	private List<Comment> commentsList;
	private long pollInterval = 60000; //time between polls in ms
	private Timer pollTimer;
	
	public CommentUpdateService(GitHubClientExtended client, int issueId, List<Comment> list) {
		super(client);
		this.issueId = issueId;
		this.commentsList = list;
		lastCheckTime = new Date();
	}
	
	private Map<String, String> createUpdatedCommentsParams(){
		Map<String, String> params = new HashMap<String, String>();
		params.put("since", getFormattedDate(lastCheckTime));
		return params;
	}

	@Override
	protected GitHubRequest createUpdatedRequest(IRepositoryIdProvider repoId){
		GitHubRequest request = new GitHubRequest();
		String path = SEGMENT_REPOS + "/" + repoId.generateId() + SEGMENT_ISSUES
				+ "/" + issueId + SEGMENT_COMMENTS;
		request.setUri(path);
		request.setParams(createUpdatedCommentsParams());
		request.setResponseContentType(CONTENT_TYPE_JSON);
		request.setType(new TypeToken<Comment>(){}.getType());
		request.setArrayType(new TypeToken<ArrayList<Comment>>(){}.getType());
		return request;
	}
	
	private void updateCommentsInList(Comment comment){
		comment.setBodyHtml(ServiceManager.getInstance().getMarkupForComment(comment));
		int index = getCommentsInListWithId(comment.getId());
		if(index != -1){
			commentsList.set(index, comment);
		}else{
			commentsList.add(0, comment);
		}
	}
	
	private int getCommentsInListWithId(long id){
		for(int i = 0; i < commentsList.size(); i++){
			if(commentsList.get(i).getId() == id){
				return i;
			}
		}
		return -1;
	}
	
	protected void updateCachedComments(IRepositoryIdProvider repoId){
		List<Comment> updatedComments = super.getUpdatedItems(repoId);
		if(!updatedComments.isEmpty()){
			updatedComments.stream().forEach(comment -> updateCommentsInList(comment));
		}
	}
	
	public void startCommentsListUpdate(){
		stopCommentsListUpdate();
		pollTimer = new Timer();
		TimerTask pollTask = new TimerTask(){
			@Override
			public void run() {
				updateCachedComments(ServiceManager.getInstance().getRepoId());
			}
		};
		pollTimer.scheduleAtFixedRate(pollTask, 0, pollInterval);
	}
	
	public void stopCommentsListUpdate(){
		if(pollTimer != null){
			pollTimer.cancel();
		}
	}
}
