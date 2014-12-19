package ui.issuecolumn;

import ui.UI;
import util.events.IssueCreatedEvent;
import util.events.IssueCreatedEventHandler;
import util.events.IssueSelectedEvent;
import util.events.IssueSelectedEventHandler;

/**
 * A abstract component in charge of creating, displaying, and enabling edits of issues.
 * In its current state it is implemented via the Selenium-controlled BrowserComponent.
 *
 * Similar to ui.issuepanel.expanded.IssueCommentsDisplay, but this component
 * is owned by the ColumnControl, which is not recreated on issue selection, and thus
 * its constructor does not contain a reference to a particular issue. Instead
 * it takes ownership of issues via events.
 */
public class IssueDetailsComponent {

	private UI ui;

	public IssueDetailsComponent(UI ui) {
		this.ui = ui;
		ui.registerEvent(new IssueSelectedEventHandler() {
			@Override public void handle(IssueSelectedEvent e) {
				showIssue(e.id);
			}
		});
		ui.registerEvent(new IssueCreatedEventHandler() {
			@Override public void handle(IssueCreatedEvent e) {
				ui.getBrowserComponent().newIssue();
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
