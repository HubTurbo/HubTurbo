package ui;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
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

//		Button b1 = new Button("Admin client");
//		b1.setMaxWidth(Double.MAX_VALUE);

//		VBox content = new VBox();
//		content.getChildren().addAll(b1);

//		Button b2 = new Button("Admin user");
//		b2.setMaxWidth(Double.MAX_VALUE);
//
//		Button b3 = new Button("blah");
//		b3.setMaxWidth(Double.MAX_VALUE);
		
		Label name = new Label(issue.getTitle());
		
		VBox layout = new VBox();
		layout.getChildren().add(name);

//		setAnimated(false);
		graphicProperty().bind(Bindings.when(expandedProperty()).then(layout).otherwise(layout));
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		setExpanded(false);

//		TitledPane adminPane2 = new TitledPane("Administration", content);
//		adminPane2.setAnimated(false);
//		adminPane2.graphicProperty().bind(
//				Bindings.when(adminPane2.expandedProperty()).then(b1)
//						.otherwise(b2));
//		adminPane2.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
//
//		adminPane.setContent(adminPane2);
		// for (int i = 0; i < imageNames.length; i++) {
		// tps[i] = new TitledPane(, pics[i]);
		// }
		// accordion.getPanes().addAll(tps);
		// accordion.setExpandedPane(tps[0]);

		// listView = new ListView<>();
		// setupListView();
//		getChildren().add(adminPane);
	}
}
