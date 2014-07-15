package ui;

import service.ServiceManager;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class GlobalButtonPanel extends VBox {

	public GlobalButtonPanel(ColumnControl columns) {

		Button addColumn = new Button("\u271A");
		addColumn.setStyle("-fx-font-size: 14pt;");
		addColumn.setOnMouseClicked(columns::addColumnEvent);
		
		Button refresh = new Button("F5");
		refresh.setStyle("-fx-font-size: 12pt;");
		refresh.setOnMouseClicked(e -> {
			ServiceManager.getInstance().restartModelUpdate();
			columns.refresh();
		});
		
		getChildren().addAll(addColumn, refresh);
	}
	
}
