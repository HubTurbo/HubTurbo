package ui;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Model;

import org.controlsfx.control.NotificationPane;

import command.TurboCommandExecutor;

import filter.FilterExpression;

public class ColumnControl extends HBox {

	private final Stage stage;
	private final Model model;
	private final NotificationPane notificationPane;
	private final SidePanel sidePanel;
	
	private TurboCommandExecutor dragAndDropExecutor;

	public ColumnControl(Stage stage, Model model, NotificationPane notificationPane, SidePanel sidePanel) {
		this.stage = stage;
		this.model = model;
		this.sidePanel = sidePanel;
		this.notificationPane = notificationPane;
		this.dragAndDropExecutor = new TurboCommandExecutor();
		
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
		Column panel = new IssuePanel(stage, model, this, sidePanel, getChildren().size(), dragAndDropExecutor);
		getChildren().add(panel);
		panel.setItems(model.getIssues());
		return this;
	}

	public Column getColumn(int index) {
		return (Column) getChildren().get(index);
	}
	
	public void closeColumn(int index) {
		getChildren().remove(index);
		int i = 0;
		for (Node c : getChildren()) {
			((Column) c).updateIndex(i++);
		}
	}
	
	public void toggleColumn(int index) {
		Column column;
		Column current = (Column) getChildren().get(index);
		FilterExpression currentFilterExpr = current.getCurrentFilterExpression();
		System.out.println(currentFilterExpr);
		if (current instanceof HierarchicalIssuePanel) {
			column = new IssuePanel(stage, model, this, sidePanel, index, dragAndDropExecutor);
		} else {
			column = new HierarchicalIssuePanel(stage, model, this, sidePanel, index, dragAndDropExecutor);
		}
		column.setItems(model.getIssues());
		column.filter(currentFilterExpr);
		System.out.println(column.getCurrentFilterExpression());
		getChildren().set(index, column);
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
