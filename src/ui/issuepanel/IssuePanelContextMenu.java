package ui.issuepanel;

import ui.issuecolumn.ColumnControl;
import ui.sidepanel.SidePanel;
import util.Browse;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import model.Model;
import model.TurboIssue;

public class IssuePanelContextMenu {

	private Model model;
	private SidePanel sidePanel;
//	private ColumnControl parentColumnControl;
	private TurboIssue issue = null;

	public IssuePanelContextMenu(Model model, SidePanel sidePanel, ColumnControl parentColumnControl, TurboIssue issue) {
		this.model = model;
		this.sidePanel = sidePanel;
//		this.parentColumnControl = parentColumnControl;
		this.issue = issue;
	}
	
	public ContextMenu get() {
		ContextMenu menu = new ContextMenu();
		
		MenuItem viewOnGitHub = new MenuItem("View on GitHub");
		viewOnGitHub.setOnAction(e -> {
			Browse.browse(issue.getHtmlUrl());
		});
		MenuItem newChild = new MenuItem("New Child Issue");
		newChild.setOnAction(e -> {
			TurboIssue newIssue = new TurboIssue("", "", model);
			assert issue != null;
			newIssue.setParentIssue(issue.getId());
			sidePanel.triggerIssueCreate(newIssue);
		});
		menu.getItems().addAll(viewOnGitHub, newChild);
		
		return menu;
	}
	
}
