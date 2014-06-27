package ui;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ParentIssuesDisplayBox extends HBox {
	
	private String defaultText;
	private ObservableList<Integer> issueNumbers = null;
	
	public ParentIssuesDisplayBox(String defaultText, ObservableList<Integer> items) {
		this.defaultText = defaultText;
		setListableItems(items);
		setup();
	}
	
	private void setup() {
		setStyle(UI.STYLE_BORDERS_FADED);
	}

	private void setListableItems(ObservableList<Integer> issueNumbers) {
		this.issueNumbers = issueNumbers;
		
		issueNumbers.addListener(new ListChangeListener<Integer>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Integer> arg0) {
				update();
			}
		});
		
		update();
	}
	
	private void update() {
		Label label;
		if (issueNumbers.size() == 0) {
			label = new Label(defaultText);
			label.setStyle(UI.STYLE_FADED + "-fx-padding: 5;");
		} else {
			StringBuilder parentSB = new StringBuilder();
			for (Integer p : issueNumbers) {
				parentSB.append("#" + p);
				parentSB.append(", ");
			}
			if (parentSB.length() != 0) parentSB.delete(parentSB.length()-2, parentSB.length());
			label = new Label(parentSB.toString());
			label.setStyle("-fx-padding: 5;");
		}
		getChildren().clear();
		getChildren().add(label);
	}
	
}
