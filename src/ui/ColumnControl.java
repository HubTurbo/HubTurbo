package ui;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Model;

import org.controlsfx.control.NotificationPane;

import util.ConfigFileHandler;
import util.SessionConfigurations;

import command.TurboCommandExecutor;

import filter.FilterExpression;


public class ColumnControl extends HBox {

	private final Stage stage;
	private final Model model;
	private final NotificationPane notificationPane;
	private final SidePanel sidePanel;
	
	private TurboCommandExecutor dragAndDropExecutor;
	
	private SessionConfigurations sessionConfig;

	public ColumnControl(Stage stage, Model model, NotificationPane notificationPane, SidePanel sidePanel) {
		this.stage = stage;
		this.model = model;
		this.sidePanel = sidePanel;
		this.notificationPane = notificationPane;
		this.dragAndDropExecutor = new TurboCommandExecutor();
		this.sessionConfig = ConfigFileHandler.loadSessionConfig();
	}
	
	public void resumeColumns() {
		List<String> filters = sessionConfig.getFiltersFromPreviousSession(model.getRepoId());
		if (filters != null && !filters.isEmpty()) {
			for (String filter : filters) {
				addColumn().filterByString(filter);
			}
		} else {
			addColumn();
		}
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

	public void addColumnEvent(MouseEvent e) {
		addColumn();
	}
	
	public Column addColumn() {
		Column panel = new IssuePanel(stage, model, this, sidePanel, getChildren().size(), dragAndDropExecutor);
		getChildren().add(panel);
		panel.setItems(model.getIssues());
		return panel;
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
		if (current instanceof HierarchicalIssuePanel) {
			column = new IssuePanel(stage, model, this, sidePanel, index, dragAndDropExecutor);
		} else {
			column = new HierarchicalIssuePanel(stage, model, this, sidePanel, index, dragAndDropExecutor);
		}
		column.setItems(model.getIssues());
		column.filter(currentFilterExpr);
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

	public void saveSession() {
		List<String> sessionFilters = new ArrayList<String>();
		getChildren().forEach(child -> {
			String filter = ((Column) child).getCurrentFilterExpression().toString();
			sessionFilters.add(filter);
		});
		sessionConfig.setFiltersForNextSession(model.getRepoId(), sessionFilters);
		ConfigFileHandler.saveSessionConfig(sessionConfig);
	}
}
