package ui;

import java.util.ArrayList;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import logic.LogicFacade;
import logic.TurboIssue;

public class ColumnControl extends HBox {

	private final Stage stage;
	private final LogicFacade logic;

	private ArrayList<IssuePanel> panels;
	
	public ColumnControl(Stage stage, LogicFacade logic) {
		this.stage = stage;
		this.logic = logic;
		
		panels = new ArrayList<>();
		
		addColumn();
		addSampleIssues();
	}
	
	public void loadIssues(ObservableList<TurboIssue> issues) {
		for (IssuePanel panel : panels) {
			panel.setItems(issues);
		}
	}

	public ColumnControl addColumn() {
		IssuePanel panel = new IssuePanel(stage, logic);
		panels.add(panel);
		getChildren().add(panel);
		
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
			panels.remove(panels.size() - 1 - numberToRemove, panels.size() - 1);
//			columns.remove(panels.size() - 1 - numberToRemove, panels.size() - 1);
//			columns.remove
		}

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
