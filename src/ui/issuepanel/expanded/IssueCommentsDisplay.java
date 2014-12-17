package ui.issuepanel.expanded;

import model.TurboIssue;
import ui.UI;

/**
 * An abstraction for the display pane that shows comments.
 * In its current state it is implemented via a Selenium-controlled
 * BrowserComponent.
 */
public class IssueCommentsDisplay {

	private UI ui;
	private TurboIssue issue;

	public IssueCommentsDisplay(UI ui, TurboIssue current) {
		this.ui = ui;
		this.issue = current;
	}

	/**
	 * Invoked when the Details button is clicked.
	 */
	public void toggle() {
		// Show the driver
		if (!ui.toggleExpandedWidth()) {
			ui.getBrowserComponent().showIssue(issue.getId());
		} else {
			// Do nothing
		}
	}
}
