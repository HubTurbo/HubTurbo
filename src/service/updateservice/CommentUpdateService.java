package service.updateservice;

import static org.eclipse.egit.github.core.client.IGitHubConstants.CONTENT_TYPE_JSON;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COMMENTS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_ISSUES;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.PagedRequest;

import service.GitHubClientExtended;
import service.ServiceManager;

import com.google.gson.reflect.TypeToken;

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

	@Override
	protected PagedRequest<Comment> createUpdatedRequest(IRepositoryIdProvider repoId){
		PagedRequest<Comment> request = new PagedRequest<Comment>();
		String path = SEGMENT_REPOS + "/" + repoId.generateId() + SEGMENT_ISSUES
				+ "/" + issueId + SEGMENT_COMMENTS;
		request.setUri(path);
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
		List<Comment> updatedComments = super.getUpdatedItems(repoId); //updateComments is the list of all comments for the issue.
		if(!updatedComments.isEmpty()){
			List<Comment> removed = getRemovedComments(updatedComments);
			commentsList.removeAll(removed);
			updatedComments.stream().forEach(comment -> updateCommentsInList(comment));
			updateGlobalCachedCommentsForIssue(updatedComments);
		}
	}
	
	private void updateGlobalCachedCommentsForIssue(List<Comment> comments){
		ServiceManager.getInstance().getModel().cacheCommentsListForIssue(comments, issueId);
	}
	
	private List<Comment> getRemovedComments(List<Comment> updatedComments){
		//TODO: optimise for sorted list
		return commentsList.stream().filter(item -> !hasComment(updatedComments, item)).collect(Collectors.toList());
	}
	
	private boolean hasComment(List<Comment> comments, Comment comment){
		for(Comment item: comments){
			if(comment.getId() == item.getId()){
				return true;
			}
		}
		return false;
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
	
	public void restartCommentsListUpdate(){
		stopCommentsListUpdate();
		startCommentsListUpdate();
	}
	
	public void stopCommentsListUpdate(){
		if(pollTimer != null){
			pollTimer.cancel();
			pollTimer = null;
		}
	}
}
