package ui.issuecolumn;

import javafx.application.Platform;
import service.TickingTimer;
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

import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

	private final int BROWSER_REQUEST_DELAY = 400; //milliseconds
	private TickingTimer timer;
	private Optional<Integer> nextIssueId = Optional.empty();

	public UIBrowserBridge(UI ui) {
		this.ui = ui;
		timer = createTickingTimer();
		timer.start();
		ui.registerEvent(new IssueSelectedEventHandler() {
			@Override public void handle(IssueSelectedEvent e) {
				nextIssueId = Optional.of(e.id);
				timer.restart();
				if (timer.isPaused()) {
					timer.resume();
				}
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
				if (ui.getCommandLineArgs().containsKey(UI.ARG_UPDATED_TO)) {
					ui.getBrowserComponent().showChangelog(ui.getCommandLineArgs().get(UI.ARG_UPDATED_TO));
				}
			}
		});
		ui.registerEvent(new LabelCreatedEventHandler() {
			@Override public void handle(LabelCreatedEvent e) {
				ui.getBrowserComponent().newLabel();
			}
		});
		ui.registerEvent(new MilestoneCreatedEventHandler() {
			@Override public void handle(MilestoneCreatedEvent e) {
				ui.getBrowserComponent().newMilestone();
			}
		});
	}

	private TickingTimer createTickingTimer() {
		return new TickingTimer("Browser Request Delay Timer", BROWSER_REQUEST_DELAY, integer -> {
			// do nothing for each tick
		}, () -> {
			if (nextIssueId.isPresent()) {
				Platform.runLater(() -> {
					ui.getBrowserComponent().showIssue(nextIssueId.get());
				});
			}
			timer.pause();
		}, TimeUnit.MILLISECONDS);
	}
}
