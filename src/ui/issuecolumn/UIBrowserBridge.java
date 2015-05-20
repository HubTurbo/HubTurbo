package ui.issuecolumn;

import ui.UI;
import util.events.IssueCreatedEvent;
import util.events.IssueCreatedEventHandler;
import util.events.IssueSelectedEvent;
import util.events.IssueSelectedEventHandler;
import util.events.LabelCreatedEvent;
import util.events.LabelCreatedEventHandler;
import util.events.LoginEvent;
import util.events.LoginEventHandler;
import util.events.MilestoneCreatedEvent;
import util.events.MilestoneCreatedEventHandler;

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

	public UIBrowserBridge(UI ui) {
		ui.registerEvent((IssueSelectedEventHandler) e ->
			ui.getBrowserComponent().showIssue(e.repoId, e.id));

		ui.registerEvent((IssueCreatedEventHandler) e ->
			ui.getBrowserComponent().newIssue());

		ui.registerEvent((LoginEventHandler) e -> {
			ui.getBrowserComponent().login();
			if (ui.getCommandLineArgs().containsKey(UI.ARG_UPDATED_TO)) {
				ui.getBrowserComponent().showChangelog(ui.getCommandLineArgs().get(UI.ARG_UPDATED_TO));
			}
		});

		ui.registerEvent((LabelCreatedEventHandler) e ->
			ui.getBrowserComponent().newLabel());

		ui.registerEvent((MilestoneCreatedEventHandler) e ->
			ui.getBrowserComponent().newMilestone());
	}
}
