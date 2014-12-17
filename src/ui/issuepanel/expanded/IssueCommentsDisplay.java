package ui.issuepanel.expanded;

import ui.UI;

public class IssueCommentsDisplay {

	private UI ui;	

	public IssueCommentsDisplay(UI ui) {
		this.ui = ui;
	}

	public void toggle() {
		// Show the driver
		if (!ui.toggleExpandedWidth()) {
			// TODO needs ref to current issue to do stuff
			ui.getBrowserComponent().showIssue(1);
		} else {
			// Do nothing; leave the driver in the background
		}
	}
}
