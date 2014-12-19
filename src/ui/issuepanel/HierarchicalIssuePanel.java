package ui.issuepanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;
import ui.UI;
import ui.issuecolumn.Column;
import ui.issuecolumn.ColumnControl;
import ui.sidepanel.SidePanel;

import command.TurboCommandExecutor;

public class HierarchicalIssuePanel extends Column {

//	private final Stage mainStage;
	private final Model model;
	private final ColumnControl parentColumnControl;
//	private final int columnIndex;
	private final SidePanel sidePanel;
	private final UI ui;
	
	VBox content = new VBox();
	ScrollPane scrollPane = new ScrollPane();
	
	public HierarchicalIssuePanel(UI ui, Stage mainStage, Model model, ColumnControl parentColumnControl, SidePanel sidePanel, int columnIndex, TurboCommandExecutor dragAndDropExecutor, boolean isSearchPanel) {
		super(ui, mainStage, model, parentColumnControl, sidePanel, columnIndex, dragAndDropExecutor, isSearchPanel);
//		this.mainStage = mainStage;
		this.model = model;
		this.parentColumnControl = parentColumnControl;
//		this.columnIndex = columnIndex;
		this.sidePanel = sidePanel;
		this.ui = ui;

		VBox.setVgrow(scrollPane, Priority.ALWAYS);
		scrollPane.setContent(content);
		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		getChildren().add(scrollPane);

		refreshItems();
	}
	
	@Override
	public void deselect() {
		// Nothing here
	}
	
	@Override
	public void refreshItems() {
		super.refreshItems();
		
		content.getChildren().clear();
		
		// Build an adjacency list of issues and their children
		
		HashMap<Integer, ArrayList<TurboIssue>> childrenAdjList = new HashMap<>();
		ObservableList<? extends TurboIssue> allIssues = getIssueList().getSource();
		for (TurboIssue issue : allIssues) {
			int parentId = issue.getParentIssue();

			// A top-level issue contributes no information
			if (parentId == -1) continue;
			
			if (childrenAdjList.get(parentId) == null) {
				childrenAdjList.put(parentId, new ArrayList<>());
			}
			childrenAdjList.get(parentId).add(issue);
		}
		
		// Create all the items
		
		ObservableList<TurboIssue> filteredIssues = getIssueList();
		HashMap<Integer, HierarchicalIssuePanelItem> items = new HashMap<>();
		
		// Make a pass through the list of filtered issues, creating items
		// for each of them, plus their parents and children
		ArrayList<TurboIssue> created = new ArrayList<>();
		
		for (TurboIssue issue : filteredIssues) {
//			System.out.println("at issue " + issue.getId());
			// Do parents
			TurboIssue current = issue;
			do {
				if (!items.containsKey(current.getId())) {
					items.put(current.getId(), new HierarchicalIssuePanelItem(current));
					created.add(current);
				}
//				if (current.getId() != issue.getId()) System.out.println("parent " + current.getId());
				current = model.getIssueWithId(current.getParentIssue());
			} while (current != null);
			
			// Do children
			Stack<TurboIssue> stack = new Stack<>();
			stack.push(issue);
			while (stack.size() > 0) {
				TurboIssue ish = stack.pop();
//				if (ish.getId() != issue.getId()) System.out.println("child " + ish.getId());
				if (!items.containsKey(ish.getId())) {
					items.put(ish.getId(), new HierarchicalIssuePanelItem(ish));
					created.add(ish);
				}
				if (childrenAdjList.containsKey(ish.getId())) {
					for (TurboIssue temp : childrenAdjList.get(ish.getId())) {
						stack.push(temp);
					}
				}
			}
		}
//		System.out.println("created " + created.size() + " " + created);
		
		// Make another pass, add those that are children to their parents
		for (TurboIssue issue : created) {
			if (issue.getParentIssue() == -1) {
//				System.out.println("added " + issue.getId());
				content.getChildren().add(items.get(issue.getId()));
			}
			else {
//				System.out.println("parented " + issue.getId());
				assert items.get(issue.getParentIssue()) != null;
				items.get(issue.getParentIssue()).addChild(items.get(issue.getId()));
			}
			
			HierarchicalIssuePanelItem item = items.get(issue.getId());
			item.setExpanded(true);
			final TurboIssue thisIssue = issue;
			item.setOnMouseClicked(e -> sidePanel.triggerIssueEdit(thisIssue, true));
			item.setContextMenu(new IssuePanelContextMenu(model, sidePanel, parentColumnControl, issue).get());
		}
	}
}
