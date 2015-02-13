package service.updateservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Comment;

import service.ServiceManager;
import ui.UI;


public class CommentDownloader {
	
	private static final Logger logger = LogManager.getLogger(CommentDownloader.class.getName());

	private final ServiceManager serviceManager;

	public CommentDownloader(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	public void download() {
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
