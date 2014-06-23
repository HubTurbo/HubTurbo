package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.controlsfx.control.CheckListView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Listable;

public class FilterableCheckboxList implements Dialog<List<Integer>> {

	Stage parentStage;
	FilteredList<String> objects;

	CompletableFuture<List<Integer>> response;
	
	// State variables
	
	int previouslyChecked = -1;
	int previousState = 0;
	
	// Disables the callback to prevent an infinite loop (since
	// updating the check state updates the check state)
	boolean disabled = false;
	
	// Also disables the callback. Throttles its activation
	// to once per change
	boolean oneActivation = false;

	public FilterableCheckboxList(Stage parentStage,
			ObservableList<Listable> objects) {
		this.parentStage = parentStage;
		ObservableList<String> stringRepresentations = FXCollections
				.observableArrayList(objects.stream()
						.map((obj) -> obj.getListName())
						.collect(Collectors.toList()));
		this.objects = new FilteredList<>(stringRepresentations, p -> true);

		response = new CompletableFuture<>();
	}

	public CompletableFuture<List<Integer>> show() {
		showDialog();
		return response;
	}

	private void showDialog() {

		TextField searchField = new TextField();
		searchField.setPromptText("Search");

		CheckListView<String> checkListView = new CheckListView<>(objects);
		checkListView.getSelectionModel()
				.setSelectionMode(SelectionMode.SINGLE);

		initialCheckedState.forEach((i) -> checkListView.getCheckModel()
				.select(i));

		if (!multipleSelection) {
			assert initialCheckedState.size() == 1
					|| initialCheckedState.size() == 0;
			if (initialCheckedState.size() == 1) {
				previousState = 1;
				previouslyChecked = initialCheckedState.get(0);
			}
		}
		
		// getSelectionModel().getSelectedItems() can also have a listener

		/**
		 * A small state machine to get around API oddities.
		 * 
		 * The goal is to implement the `multipleSelection` flag, which when
		 * false allows only a single checkbox to be checked.
		 * 
		 * Basically there are 4 states which a given pair of checkboxes can be in:
		 * 00, 01, 10, 11. This is exhaustive because we only need a second checked box
		 * to reduce the state back to 01 or 10. State 11 technically doesn't exist
		 * because we transition as soon as we enter it. Transitions: 1 bit at a time.
		 * 
		 * previousState and previouslyChecked are used to track this information.
		 * c.wasAdded() || c.wasRemoved(), disabled, and oneActivation are for throttling
		 * the execution of the callback in various ways.
		 */
		
		checkListView.getCheckModel().getSelectedItems()
				.addListener(new ListChangeListener<String>() {
					@Override
					public void onChanged(
							ListChangeListener.Change<? extends String> c) {

						while (c.next()) {
							if (!multipleSelection
									&& (c.wasAdded() || c.wasRemoved())
									&& !disabled && !oneActivation) {
								List<Integer> currentlyChecked = checkListView
										.getCheckModel().getSelectedIndices();
								oneActivation = true;

								if (currentlyChecked.size() == 1) {
									assert previousState != 1;
									previouslyChecked = currentlyChecked.get(0);
									previousState = 1;
								} else if (currentlyChecked.size() == 2) {
									assert previousState == 1;
									assert previouslyChecked != -1;

									// There is no state 2: it is always skipped
									previousState = 1;

									int newlyChecked = previouslyChecked == currentlyChecked
											.get(0) ? currentlyChecked.get(1)
											: currentlyChecked.get(0);

									previouslyChecked = newlyChecked;
									disabled = true;
									checkListView.getCheckModel()
											.clearAndSelect(newlyChecked);
									disabled = false;
								} else {
									assert currentlyChecked.size() == 0;
									assert previousState == 1;
									previousState = 0;
									previouslyChecked = -1;
								}
							}
						}
						oneActivation = false;
					}
				});

		Stage stage = new Stage();

		Button close = new Button("Close");
		VBox.setMargin(close, new Insets(5));
		close.setOnAction((e) -> {
			response.complete(checkListView.getCheckModel()
					.getSelectedIndices());
			stage.hide();
		});

		VBox layout = new VBox();
		layout.setAlignment(Pos.CENTER_RIGHT);
		layout.getChildren().addAll(searchField, checkListView, close);

		Scene scene = new Scene(layout, 400, 300);

		stage.setTitle(windowTitle);
		stage.setScene(scene);

		stage.setOnCloseRequest((e) -> {
			response.complete(checkListView.getCheckModel()
					.getSelectedIndices());
		});

		Platform.runLater(() -> stage.requestFocus());

		stage.initOwner(parentStage);
		stage.initModality(Modality.APPLICATION_MODAL);

		// stage.setX(parentStage.getX());
		// stage.setY(parentStage.getY());

		stage.show();
	}

	List<Integer> initialCheckedState = new ArrayList<>();

	public List<Integer> getInitialCheckedState() {
		return initialCheckedState;
	}

	public FilterableCheckboxList setInitialCheckedState(
			List<Integer> initialCheckedState) {
		this.initialCheckedState = initialCheckedState;
		return this;
	}

	String windowTitle = "";

	public String getWindowTitle() {
		return windowTitle;
	}

	public FilterableCheckboxList setWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
		return this;
	}

	boolean multipleSelection = true;

	public boolean getMultipleSelection() {
		return multipleSelection;
	}

	public FilterableCheckboxList setMultipleSelection(boolean multipleSelection) {
		this.multipleSelection = multipleSelection;
		return this;
	}
}
