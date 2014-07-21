package ui;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import model.Model;
import model.TurboIssue;

public class IssuePanelContextMenu {

	private Model model;
	private SidePanel sidePanel;
//	private ColumnControl parentColumnControl;
	private int issueIndex = -1;

	public IssuePanelContextMenu(Model model, SidePanel sidePanel, ColumnControl parentColumnControl, int issueIndex) {
		this.model = model;
		this.sidePanel = sidePanel;
//		this.parentColumnControl = parentColumnControl;
		this.issueIndex  = issueIndex;
	}
	
	public ContextMenu get() {
		ContextMenu menu = new ContextMenu();
		
		MenuItem newChild = new MenuItem("New Child Issue");
		newChild.setOnAction(e -> {
			TurboIssue issue = new TurboIssue("", "", model);
			assert issueIndex != -1;
			issue.setParentIssue(issueIndex);
			sidePanel.triggerIssueCreate(issue);
		});
		menu.getItems().addAll(newChild);
		
		return menu;
	}
	
}
