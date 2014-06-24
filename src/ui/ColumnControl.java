package ui;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

public class ColumnControl extends HBox {

	private final Stage stage;
	private final Model model;

	public ColumnControl(Stage stage, Model model) {
		this.stage = stage;
		this.model = model;
				
		addColumn();
		addSampleIssues();
	}
	
	public void loadIssues() {
		for (Node node : getChildren()) {
			IssuePanel panel = (IssuePanel) node;
			panel.setItems(FXCollections.observableArrayList(model.getIssues()));
		}
	}

	public ColumnControl addColumn() {
		IssuePanel panel = new IssuePanel(stage, model);
		getChildren().add(panel);
		panel.setItems(FXCollections.observableArrayList(model.getIssues()));
		
		return this;
	}

	public ColumnControl setColumnCount(int to) {
		// TODO the panels aren't ordered in insertion order? watch out for that

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

//		System.out.println(panels);

		return this;
	}

	private void addSampleIssues() {
	
	//		columns = new HBox();
	
	//		col1 = new IssuePanel(mainStage, logic);
	//		col2 = new IssuePanel(mainStage, logic);
	//		IssuePanel col3 = new IssuePanel(mainStage, logic);
	//
	//		test = new TurboIssue("issue one", "description one");
	//		test.getLabels().addAll(new TurboLabel("bug"),
	//				new TurboLabel("thisisalonglabel"));
	//		TurboIssue two = new TurboIssue("issue two", "desc two");
	//		TurboIssue three = new TurboIssue("issue two", "desc three");
	//		TurboIssue four = new TurboIssue("issue four", "desc four");
	//		four.getLabels().addAll(new TurboLabel("request"),
	//				new TurboLabel("feature"));
	//		TurboIssue five = new TurboIssue("issue five", "desc five");
	//
	//		col1.getItems().add(test);
	//		col1.getItems().add(two);
	//		col1.getItems().add(three);
	//		col1.getItems().add(four);
	//		col1.getItems().add(five);
	//
	//		col2.getItems().add(test);
	//
	//		columns.getChildren().addAll(col1, col2, col3);
	
		}
}
