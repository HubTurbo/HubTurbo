package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.ModelFacade;
import model.TurboIssue;

public class ColumnControl extends HBox {

	private final Stage stage;
	private final ModelFacade logic;

	// TODO remove this once caching is done logic-side
	ObservableList<TurboIssue> issues = null;

	public ColumnControl(Stage stage, ModelFacade logic) {
		this.stage = stage;
		this.logic = logic;
				
		addColumn();
		addSampleIssues();
	}
	
	public void loadIssues() {
		// TODO remove this once caching is done logic-side
		if (issues == null) issues = FXCollections.observableArrayList(logic.getIssues());

		for (Node node : getChildren()) {
			IssuePanel panel = (IssuePanel) node;
			panel.setItems(issues);
		}
	}

	public ColumnControl addColumn() {
		IssuePanel panel = new IssuePanel(stage, logic);
		getChildren().add(panel);
//		if (issues != null) panel.setItems(issues); // TODO change once caching is done
		
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
