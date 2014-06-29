package util;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;

import org.eclipse.egit.github.core.Issue;

import model.Model;

public class ModelUpdater {
	private Model model;
	private IssueUpdateService issueUpdateService;
	private long pollInterval = 60000; //time between polls in ms
	private Timer pollTimer;
	
	public ModelUpdater(GitHubClientExtended client, Model model){
		this.model = model;
		this.issueUpdateService = new IssueUpdateService(client);
	}
	
	private void updateModelIssues(){
		List<Issue> updatedIssues = issueUpdateService.getUpdatedIssues(model.getRepoId());
		Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	model.updateCachedIssues(updatedIssues);
	        }
	   });
	}
	
	public void startModelUpdate(){
		if(pollTimer != null){
			stopModelUpdate();
		}
		pollTimer = new Timer();
		TimerTask pollTask = new TimerTask(){
			@Override
			public void run() {
				updateModelIssues();
			}
		};
		pollTimer.scheduleAtFixedRate(pollTask, 0, pollInterval);
	}
	
	public void stopModelUpdate(){
		if(pollTimer != null){
			pollTimer.cancel();
			pollTimer = null;
		}
	}
}
