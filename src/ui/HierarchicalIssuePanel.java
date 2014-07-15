package ui;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

public class HierarchicalIssuePanel extends Columnable {

//	private static final String NO_FILTER = "No Filter";
//	public static final filter.Predicate EMPTY_PREDICATE = new filter.Predicate();

	private final Stage mainStage;
	private final Model model;
	private final ColumnControl parentColumnControl;
	private final int columnIndex;
	private final SidePanel sidePanel;
	
//	private ListView<TurboIssue> listView;
	private ObservableList<TurboIssue> issues;
//	private FilteredList<TurboIssue> filteredList;
	
//	private Predicate<TurboIssue> predicate;
//	private String filterInput = "";
//	private FilterExpression currentFilterExpression = EMPTY_PREDICATE;

	public HierarchicalIssuePanel(Stage mainStage, Model model, ColumnControl parentColumnControl, SidePanel sidePanel, int columnIndex) {
		this.mainStage = mainStage;
		this.model = model;
		this.parentColumnControl = parentColumnControl;
		this.columnIndex = columnIndex;
		this.sidePanel = sidePanel;

//		getChildren().add(createTop());
		
		issues = FXCollections.observableArrayList();
//		listView = new ListView<>();
//		setupListView();
//		getChildren().add(listView);
//		predicate = p -> true;
		
		setup();
		refreshItems();
	}
	
	@SuppressWarnings("unused")
	private ChangeListener<TurboIssue> listener;
	@Override
	public void deselect() {
//		listView.getSelectionModel().clearSelection();
	}

//	public void filter(FilterExpression filter) {
//		currentFilterExpression = filter;
//
//		// This cast utilises a functional interface
//		final BiFunction<TurboIssue, Model, Boolean> temp = filter::isSatisfiedBy;
//		predicate = i -> temp.apply(i, model);
//		
//		refreshItems();
//	}
	
	@Override
	public void refreshItems() {
		
		getChildren().clear();
		
		VBox stuff = new VBox();
//		stuff.setStyle("-fx-background-color: red;");
//		VBox.setVgrow(stuff, Priority.ALWAYS);
		ScrollPane everything = new ScrollPane();
		VBox.setVgrow(everything, Priority.ALWAYS);
//		everything.setStyle("-fx-background-color: green;");
//		setStyle("-fx-background-color: blue;");
		
//		System.out.println("rf");
		for (TurboIssue issue : issues) {
			stuff.getChildren().add(new HierarchicalIssuePanelItem(issue));
		}
		
		everything.setContent(stuff);
		getChildren().add(everything);
		
//		filteredList = new FilteredList<TurboIssue>(issues, predicate);
		
//		WeakReference<HierarchicalIssuePanel> that = new WeakReference<HierarchicalIssuePanel>(this);
		
		// Set the cell factory every time - this forces the list view to update
//		listView.setCellFactory(new Callback<ListView<TurboIssue>, ListCell<TurboIssue>>() {
//			@Override
//			public ListCell<TurboIssue> call(ListView<TurboIssue> list) {
//				if(that.get() != null){
////					return new IssuePanelCell(mainStage, model, that.get(), columnIndex);
//				} else{
//					return null;
//				}
//			}
//		});
		
		// Supposedly this also causes the list view to update - not sure
		// if it actually does on platforms other than Linux...
//		listView.setItems(null);
		
//		listView.setItems(filteredList);
	}

	@Override
	public void setItems(ObservableList<TurboIssue> issues) {
		this.issues = issues;
		refreshItems();
	}

	private void setupListView() {
//		listView.getSelectionModel().selectedItemProperty().addListener(new WeakChangeListener<TurboIssue>(
//			listener = (observable, previousIssue, currentIssue) -> {
//				if (currentIssue == null) return;
//				
//				// TODO save the previous issue?
//				
//				TurboIssue oldIssue = new TurboIssue(currentIssue);
//				TurboIssue modifiedIssue = new TurboIssue(currentIssue);
//				sidePanel.displayIssue(modifiedIssue).thenApply(r -> {
//					if (r.equals("done")) {
//						System.out.println("was okay");
//						model.updateIssue(oldIssue, modifiedIssue);
//					}
//					parentColumnControl.refresh();
//					sidePanel.displayTabs();
//					return true;
//				}).exceptionally(e -> {
//					e.printStackTrace();
//					return false;
//				});
//			}));
	}

//	private Node createTop() {
//		
//		HBox filterBox = new HBox();
//		Label label = new Label(NO_FILTER);
//		label.setPadding(new Insets(3));
//		filterBox.setOnMouseClicked((e) -> {
//			(new FilterDialog(mainStage, model, filterInput)).show().thenApply(
//					filterString -> {
//						filterInput = filterString;
//						if (filterString.isEmpty()) {
//							label.setText(NO_FILTER);
//							this.filter(EMPTY_PREDICATE);
//						} else {
//				        	try {
//				        		FilterExpression filter = Parser.parse(filterString);
//				        		if (filter != null) {
//									label.setText(filter.toString());
//				                	this.filter(filter);
//				        		} else {
//									label.setText(NO_FILTER);
//				                	this.filter(EMPTY_PREDICATE);
//				        		}
//				        	} catch (ParseException ex){
//				            	label.setText("Parse error in filter: " + ex);
//				            	this.filter(EMPTY_PREDICATE);
//				        	}
//						}
//						return true;
//					})
//				.exceptionally(ex -> {
//					ex.printStackTrace();
//					return false;
//				});
//		});
//		filterBox.setAlignment(Pos.TOP_LEFT);
//		HBox.setHgrow(filterBox, Priority.ALWAYS);
//		filterBox.getChildren().add(label);
//		
//		HBox rightAlignBox = new HBox();
//	
//		Label addIssue = new Label("\u2795");
//		addIssue.setStyle("-fx-font-size: 16pt;");
//		addIssue.setOnMouseClicked((e) -> {
//			TurboIssue issue = new TurboIssue("New issue", "", model);
//			applyCurrentFilterExpressionToIssue(issue, false);
//			
//			sidePanel.displayIssue(issue).thenApply(r -> {
//				if (r.equals("done")) {
//					model.createIssue(issue);
//				}
//				parentColumnControl.refresh();
//				sidePanel.displayTabs();
//				return true;
//			}).exceptionally(ex -> {
//				ex.printStackTrace();
//				return false;
//			});
//		});
//		
//		Label closeList = new Label("\u274c");
//		closeList.setStyle("-fx-font-size: 16pt;");
//		closeList.setOnMouseClicked((e) -> {
//			parentColumnControl.closeColumn(columnIndex);
//		});
//		
//		HBox.setMargin(rightAlignBox, new Insets(0,5,0,0));
//		rightAlignBox.setSpacing(5);
//		rightAlignBox.setAlignment(Pos.TOP_RIGHT);
//		HBox.setHgrow(rightAlignBox, Priority.ALWAYS);
//		rightAlignBox.getChildren().addAll(addIssue, closeList);
//		
//		HBox topBox = new HBox();
//		topBox.setSpacing(5);
//		topBox.getChildren().addAll(filterBox, rightAlignBox);
//		
//		return topBox;
//	}

	private void setup() {
			setPrefWidth(380);
			setMaxWidth(380);
//			setVgrow(listView, Priority.ALWAYS);
	//		HBox.setHgrow(this, Priority.ALWAYS);
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
//					applyCurrentFilterExpressionToIssue(rightIssue, true);
				}
				e.setDropCompleted(success);
	
				e.consume();
			});
		}

//	private void applyCurrentFilterExpressionToIssue(TurboIssue issue, boolean updateModel) {
//		if (currentFilterExpression != EMPTY_PREDICATE) {
//			try {
//				if (currentFilterExpression.canBeAppliedToIssue()) {
//					TurboIssue clone = new TurboIssue(issue);
//					currentFilterExpression.applyTo(issue, model);
//					if (updateModel) model.updateIssue(clone, issue);
//					parentColumnControl.refresh();
//				} else {
//					throw new PredicateApplicationException("Could not apply predicate " + currentFilterExpression + ".");
//				}
//			} catch (PredicateApplicationException ex) {
//				parentColumnControl.displayMessage(ex.getMessage());
//			}
//		}
//	}
}
