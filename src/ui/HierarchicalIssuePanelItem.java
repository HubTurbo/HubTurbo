package ui;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import model.TurboIssue;

public class HierarchicalIssuePanelItem extends TitledPane {
	
	private final VBox content;
	private final TurboIssue issue;

	public HierarchicalIssuePanelItem(TurboIssue issue) {
		this.content = new VBox();
		this.issue = issue;
		setContent(content);
		setup();
	}
	
	public void addChild(HierarchicalIssuePanelItem item) {
		content.getChildren().add(item);
	}

	private void setup() {
		IssuePanelCard layout = new IssuePanelCard(issue);
//		setAnimated(false);
		graphicProperty().bind(Bindings.when(expandedProperty()).then(layout).otherwise(layout));
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		setExpanded(false);
	}
}
