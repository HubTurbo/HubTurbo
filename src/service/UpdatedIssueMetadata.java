package service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Comment;

import ui.UI;
import ui.components.HTStatusBar;


public class UpdatedIssueMetadata {
	
	private static final Logger logger = LogManager.getLogger(UpdatedIssueMetadata.class.getName());

	private final ServiceManager serviceManager;

	public UpdatedIssueMetadata(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	public void download() {
		downloadComments();
		HTStatusBar.addProgressAndDisplayMessage(ModelUpdate.PROGRESS_INTERVAL, "Updating events...");
		downloadEvents();
		HTStatusBar.addProgress(ModelUpdate.PROGRESS_INTERVAL);
	}

	public void downloadEvents() {
		int issueCount = 0;
		for (Integer issueId : UI.getInstance().getColumnControl().getUpdatedIssues()) {
			++issueCount;
			List<TurboIssueEvent> events = new ArrayList<>();
			try {
				events = serviceManager.getEvents(issueId);
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
			serviceManager.getModel().getIssueWithId(issueId).setEvents(events);
		}
		logger.info("Downloaded events for " + issueCount + " issues");
	}

	public void downloadComments() {
		int issueCount = 0;
		for (Integer issueId : UI.getInstance().getColumnControl().getUpdatedIssues()) {
			++issueCount;
			List<Comment> comments = new ArrayList<>();
			try {
				comments = serviceManager.getLatestComments(issueId);
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
			serviceManager.getModel().getIssueWithId(issueId).setComments(comments);
		}
		logger.info("Downloaded comments for " + issueCount + " issues");
	}
}
