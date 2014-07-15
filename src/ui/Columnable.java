package ui;

import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import model.TurboIssue;

public abstract class Columnable extends VBox {
	// A 'Columnable' is a JavaFX node that may act as a column (of the 
	// ColumnControl).
	
	public abstract void setItems(ObservableList<TurboIssue> items);
	public abstract void refreshItems();
	public abstract void deselect();
}
