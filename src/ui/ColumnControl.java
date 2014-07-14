package ui;

import org.controlsfx.control.NotificationPane;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Model;

public class ColumnControl extends HBox {

	private final Stage stage;
	private final Model model;
	private final NotificationPane notificationPane;

	public ColumnControl(Stage stage, Model model, NotificationPane notificationPane) {
		this.stage = stage;
		this.model = model;
		this.notificationPane = notificationPane;

		addColumn();
	}
	
	public void displayMessage(String message) {
		notificationPane.setText(message);
		notificationPane.show();
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

	public ColumnControl addColumnEvent(MouseEvent e) {
		return addColumn();
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
	
	public void closeColumn(int index) {
		getChildren().remove(index);
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
