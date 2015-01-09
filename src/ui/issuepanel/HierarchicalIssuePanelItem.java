package ui.issuepanel;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import model.TurboIssue;
import ui.issuecolumn.IssueColumn;

public class HierarchicalIssuePanelItem extends TitledPane {
	
	private final VBox content;
	private final TurboIssue issue;
	private final IssueColumn parent;

	public HierarchicalIssuePanelItem(IssueColumn parent, TurboIssue issue) {
		this.content = new VBox();
		this.issue = issue;
		this.parent = parent;
		setContent(content);
		setup();
	}
	
	public void addChild(HierarchicalIssuePanelItem item) {
		content.getChildren().add(item);
	}

	private void setup() {
		IssuePanelCard layout = new IssuePanelCard(issue, parent);
//		setAnimated(false);
		graphicProperty().bind(Bindings.when(expandedProperty()).then(layout).otherwise(layout));
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		setExpanded(false);
	}
}
