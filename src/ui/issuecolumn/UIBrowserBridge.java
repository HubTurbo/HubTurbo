package ui.issuecolumn;

import ui.UI;
import util.events.IssueCreatedEvent;
import util.events.IssueCreatedEventHandler;
import util.events.IssueSelectedEvent;
import util.events.IssueSelectedEventHandler;
import util.events.LoginEvent;
import util.events.LoginEventHandler;

/**
 * A abstract component in charge of creating, displaying, and enabling edits of issues.
 * Its function is to decouple the UI logic and events from the BrowserComponent.
 * In this sense it is similar in function to ui.issuepanel.expanded.IssueCommentsDisplay.
 * 
 * Unlike the aforementioned component is owned by the ColumnControl, which is not recreated
 * on issue selection. Thus its constructor does not contain a reference to a particular issue.
 * Instead it takes ownership of issues via events.
 */
public class UIBrowserBridge {

	private UI ui;

	public UIBrowserBridge(UI ui) {
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
		ui.registerEvent(new LoginEventHandler() {
			@Override public void handle(LoginEvent e) {
				ui.getBrowserComponent().login();
			}
		});
	}

	private void showIssue(int id) {
		if (ui.isExpanded()) {
			ui.toggleExpandedWidth();
		}
		ui.getBrowserComponent().showIssue(id);
	}
}
