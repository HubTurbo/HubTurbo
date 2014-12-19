package ui.issuecolumn;

import ui.UI;
import util.events.IssueSelectedEvent;
import util.events.IssueSelectedEventHandler;

/**
 * An abstraction for the display pane that shows comments.
 * In its current state it is implemented via a Selenium-controlled
 * BrowserComponent.
 * 
 * Very similar to ui.issuepanel.expanded.IssueCommentsDisplay, but this component
 * is owned by the ColumnControl, which is not recreated on issue selection, thus
 * its constructor does not contain a reference to a particular issue. Instead
 * it takes ownership of issues via events.
 */
public class IssueDetailsDisplay {

	private UI ui;

	public IssueDetailsDisplay(UI ui) {
		this.ui = ui;
		ui.registerEvent(new IssueSelectedEventHandler() {
			@Override public void handle(IssueSelectedEvent e) {
				showIssue(e.id);
			}
		});
	}

	private void showIssue(int id) {
		if (ui.isExpanded()) {
			ui.toggleExpandedWidth();
			ui.getBrowserComponent().showIssue(id);
		}
	}
}
