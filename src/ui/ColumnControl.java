package ui;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Model;

public class ColumnControl extends HBox {

	private final Stage stage;
	private final Model model;

	public ColumnControl(Stage stage, Model model) {
		this.stage = stage;
		this.model = model;
				
		addColumn();
	}
	
	public void refresh() {
		getChildren().forEach(child -> ((IssuePanel) child).refreshItems());
	}
	
	public void loadIssues() {
		for (Node node : getChildren()) {
			IssuePanel panel = (IssuePanel) node;
			panel.setItems(model.getIssues());
		}
	}

	public ColumnControl addColumn() {
		IssuePanel panel = new IssuePanel(stage, model, this, getChildren().size());
		getChildren().add(panel);
		panel.setItems(model.getIssues());
		
		return this;
	}

	public IssuePanel getColumn(int index) {
		return (IssuePanel) getChildren().get(index);
	}

	public ColumnControl setColumnCount(int to) {
		ObservableList<Node> panels = getChildren();
		int panelSize = panels.size();

		if (panelSize == to) {
			return this;
		}

		if (panelSize < to) {
			for (int i = 0; i < to - panelSize; i++) {
				addColumn();
			}
		} else {
			assert panels.size() > to;
			int numberToRemove = panels.size() - to;
			panels.remove(panels.size() - numberToRemove, panels.size());
		}
		
		return this;
	}
}
