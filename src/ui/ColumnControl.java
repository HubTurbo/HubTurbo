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
	private final SidePanel sidePanel;

	public ColumnControl(Stage stage, Model model, NotificationPane notificationPane, SidePanel sidePanel) {
		this.stage = stage;
		this.model = model;
		this.sidePanel = sidePanel;
		this.notificationPane = notificationPane;

		addColumn();
	}
	
	public void displayMessage(String message) {
		notificationPane.setText(message);
		notificationPane.show();
	}
	
	public void refresh() {
		getChildren().forEach(child -> ((Column) child).refreshItems());
	}
	
	public void deselect() {
		getChildren().forEach(child -> ((Column) child).deselect());
	}

	public void loadIssues() {
		for (Node node : getChildren()) {
			Column panel = (Column) node;
			panel.setItems(model.getIssues());
		}
	}

	public ColumnControl addColumnEvent(MouseEvent e) {
		return addColumn();
	}
	
	public ColumnControl addColumn() {
		Column panel = new IssuePanel(stage, model, this, sidePanel, getChildren().size());
//		Columnable panel = new HierarchicalIssuePanel(stage, model, this, sidePanel, getChildren().size());
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
