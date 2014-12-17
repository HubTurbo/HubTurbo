package ui.issuepanel.expanded;

import ui.UI;
import util.events.IssueSelectedEvent;

public class IssueCommentsDisplay {

	private UI ui;	

	public IssueCommentsDisplay(UI ui) {
		this.ui = ui;
		
		ui.registerEvent((IssueSelectedEvent e) -> {
			if (!ui.isExpanded()) {
				ui.getBrowserComponent().showIssue(e.id);
			}
		});
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
