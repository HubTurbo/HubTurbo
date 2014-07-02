package util;

import java.lang.ref.WeakReference;
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
		WeakReference<Model> modelRef = new WeakReference<Model>(model);
		Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	Model model = modelRef.get();
	        	if(model != null){
	        		model.updateCachedIssues(updatedIssues);
	        	}
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
