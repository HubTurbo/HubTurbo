package ui;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import filter.FilterExpression;
import filter.ParseException;
import filter.Parser;
import filter.PredicateApplicationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

public abstract class Column extends VBox {
	
	// A Column is a JavaFX node that is contained by a ColumnControl.
	// It is in charge of displaying a list of issues and providing functions
	// to filter it. It does not, however, specify how the list is to be
	// displayed -- that is the job of its subclasses.
	
	public static final String NO_FILTER = "No Filter";
	public static final filter.Predicate EMPTY_PREDICATE = new filter.Predicate();

	private final Stage mainStage;
	private final Model model;
	private final ColumnControl parentColumnControl;
	private final int columnIndex;
	private final SidePanel sidePanel;

	private Predicate<TurboIssue> predicate = p -> true;
	private String filterInput = "";
	protected FilterExpression currentFilterExpression = EMPTY_PREDICATE;
	protected ObservableList<TurboIssue> issues = FXCollections.observableArrayList();
	protected FilteredList<TurboIssue> filteredList = null;

	public Column(Stage mainStage, Model model, ColumnControl parentColumnControl, SidePanel sidePanel, int columnIndex) {
		this.mainStage = mainStage;
		this.model = model;
		this.parentColumnControl = parentColumnControl;
		this.columnIndex = columnIndex;
		this.sidePanel = sidePanel;

		getChildren().add(createFilterBox());
		setup();
	}
	
	private void setup() {
		setPrefWidth(380);
		setMaxWidth(380);
		
		getStyleClass().add("borders");
		
		setOnDragOver(e -> {
			if (e.getGestureSource() != this && e.getDragboard().hasString()) {
				e.acceptTransferModes(TransferMode.MOVE);
			}
		});

		setOnDragEntered(e -> {
			if (e.getDragboard().hasString()) {
				IssuePanelDragData dd = IssuePanelDragData.deserialise(e.getDragboard().getString());
				if (dd.getColumnIndex() != columnIndex) {
					getStyleClass().add("dragged-over");
				}
			}
			e.consume();
		});

		setOnDragExited(e -> {
			getStyleClass().remove("dragged-over");
			e.consume();
		});
		
		setOnDragDropped(e -> {
			Dragboard db = e.getDragboard();
			boolean success = false;
			if (db.hasString()) {
				success = true;
				IssuePanelDragData dd = IssuePanelDragData.deserialise(db.getString());
								
				// Find the right issue from its ID
				TurboIssue rightIssue = null;
				for (TurboIssue i : model.getIssues()) {
					if (i.getId() == dd.getIssueIndex()) {
						rightIssue = i;
					}
				}
				assert rightIssue != null;
				applyCurrentFilterExpressionToIssue(rightIssue, true);
			}
			e.setDropCompleted(success);

			e.consume();
		});
	}

	private Node createFilterBox() {
		HBox filterBox = new HBox();

		Label label = new Label(NO_FILTER);
		label.setPadding(new Insets(3));
		
		filterBox.setOnMouseClicked(e -> onFilterBoxClick(label));
		filterBox.setAlignment(Pos.TOP_LEFT);
		HBox.setHgrow(filterBox, Priority.ALWAYS);
		filterBox.getChildren().add(label);
		
		HBox rightAlignBox = new HBox();
	
		Label addIssue = new Label("\u2795");
		addIssue.setStyle("-fx-font-size: 16pt;");
		addIssue.setOnMouseClicked((e) -> {
			TurboIssue issue = new TurboIssue("New issue", "", model);
			applyCurrentFilterExpressionToIssue(issue, false);
			
			sidePanel.displayIssue(issue).thenApply(r -> {
				if (r.equals("done")) {
					model.createIssue(issue);
				}
				parentColumnControl.refresh();
				sidePanel.displayTabs();
				return true;
			}).exceptionally(ex -> {
				ex.printStackTrace();
				return false;
			});
		});
		
		Label closeList = new Label("\u274c");
		closeList.setStyle("-fx-font-size: 16pt;");
		closeList.setOnMouseClicked((e) -> {
			parentColumnControl.closeColumn(columnIndex);
		});
		
		HBox.setMargin(rightAlignBox, new Insets(0,5,0,0));
		rightAlignBox.setSpacing(5);
		rightAlignBox.setAlignment(Pos.TOP_RIGHT);
		HBox.setHgrow(rightAlignBox, Priority.ALWAYS);
		rightAlignBox.getChildren().addAll(addIssue, closeList);
		
		HBox topBox = new HBox();
		topBox.setSpacing(5);
		topBox.getChildren().addAll(filterBox, rightAlignBox);
		
		return topBox;
	}
	
	private void onFilterBoxClick(Label label) {
		(new FilterDialog(mainStage, model, filterInput))
			.show()
			.thenApply(filterString -> {
				filterInput = filterString;
				if (filterString.isEmpty()) {
					label.setText(NO_FILTER);
					this.filter(EMPTY_PREDICATE);
				} else {
					try {
						FilterExpression filter = Parser.parse(filterString);
						if (filter != null) {
							label.setText(filter.toString());
							this.filter(filter);
						} else {
							label.setText(NO_FILTER);
							this.filter(EMPTY_PREDICATE);
						}
					} catch (ParseException ex) {
						label.setText("Parse error in filter: " + ex);
						this.filter(EMPTY_PREDICATE);
					}
				}
				return true;
			}).exceptionally(ex -> {
				ex.printStackTrace();
				return false;
			});
	}
	
	public void filter(FilterExpression filter) {
		currentFilterExpression = filter;

		// This cast utilises a functional interface
		final BiFunction<TurboIssue, Model, Boolean> temp = filter::isSatisfiedBy;
		predicate = i -> temp.apply(i, model);
		
		refreshItems();
	}
	
	public void setItems(ObservableList<TurboIssue> items) {
		this.issues = items;
		refreshItems();
	}

	public void refreshItems() {
		filteredList = new FilteredList<TurboIssue>(issues, predicate);
	}
	
	public abstract void deselect();

	private void applyCurrentFilterExpressionToIssue(TurboIssue issue, boolean updateModel) {
		if (currentFilterExpression != EMPTY_PREDICATE) {
			try {
				if (currentFilterExpression.canBeAppliedToIssue()) {
					TurboIssue clone = new TurboIssue(issue);
					currentFilterExpression.applyTo(issue, model);
					if (updateModel) model.updateIssue(clone, issue);
					parentColumnControl.refresh();
				} else {
					throw new PredicateApplicationException("Could not apply predicate " + currentFilterExpression + ".");
				}
			} catch (PredicateApplicationException ex) {
				parentColumnControl.displayMessage(ex.getMessage());
			}
		}
	}

}
