package ui.issuepanel.expanded;

import model.TurboIssue;
import ui.UI;

public class IssueCommentsDisplay {

	private UI ui;
	private TurboIssue issue;

	public IssueCommentsDisplay(UI ui, TurboIssue current) {
		this.ui = ui;
		this.issue = current;
	}

	public void toggle() {
		// Show the driver
		if (!ui.toggleExpandedWidth()) {
			ui.getBrowserComponent().showIssue(issue.getId());
		} else {
			// Do nothing
		}
	}
}
