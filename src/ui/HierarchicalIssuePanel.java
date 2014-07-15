package ui;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

import command.TurboCommandExecutor;

public class HierarchicalIssuePanel extends Column {

//	private final Stage mainStage;
	private final Model model;
//	private final ColumnControl parentColumnControl;
//	private final int columnIndex;
//	private final SidePanel sidePanel;
	
	VBox content = new VBox();
	ScrollPane scrollPane = new ScrollPane();

//	private FilteredList<TurboIssue> filteredList;
	
//	private Predicate<TurboIssue> predicate;
//	private String filterInput = "";
//	private FilterExpression currentFilterExpression = EMPTY_PREDICATE;

	public HierarchicalIssuePanel(Stage mainStage, Model model, ColumnControl parentColumnControl, SidePanel sidePanel, int columnIndex, TurboCommandExecutor dragAndDropExecutor) {
		super(mainStage, model, parentColumnControl, sidePanel, columnIndex, dragAndDropExecutor);
//		this.mainStage = mainStage;
		this.model = model;
//		this.parentColumnControl = parentColumnControl;
//		this.columnIndex = columnIndex;
//		this.sidePanel = sidePanel;

		VBox.setVgrow(scrollPane, Priority.ALWAYS);
		scrollPane.setContent(content);
		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		getChildren().add(scrollPane);

		refreshItems();
	}
	
	@Override
	public void deselect() {
		
		// TODO provide something here
//		listView.getSelectionModel().clearSelection();
	}
	
	@Override
	public void refreshItems() {
		super.refreshItems();
		
		content.getChildren().clear();
		
		// Create all the items
		
		FilteredList<TurboIssue> filteredIssues = getFilteredList();
		HashMap<Integer, HierarchicalIssuePanelItem> items = new HashMap<>();
		
		// Make a pass through the list of filtered issues, creating items
		// for each of them, plus their parents
		ArrayList<TurboIssue> created = new ArrayList<>();
		
		for (TurboIssue issue : filteredIssues) {
			TurboIssue current = issue;
			do {
				if (items.get(current.getId()) == null) {
					items.put(current.getId(), new HierarchicalIssuePanelItem(current));
					created.add(current);
				}
				current = model.getIssueWithId(current.getParentIssue());
			} while (current != null);
		}
		
		// Make another pass, add those that are children to their parents
		for (TurboIssue issue : created) {
			if (issue.getParentIssue() == -1) {
				content.getChildren().add(items.get(issue.getId()));
			}
			else {
				assert items.get(issue.getParentIssue()) != null;
				items.get(issue.getParentIssue()).addChild(items.get(issue.getId()));
			}
		}
	}
}
