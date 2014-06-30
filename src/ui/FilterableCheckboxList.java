package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.controlsfx.control.CheckListView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Listable;

public class FilterableCheckboxList implements Dialog<List<Integer>> {

	private final Stage parentStage;
	private FilteredList<String> objects;

	private CompletableFuture<List<Integer>> response;
	
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
		
		CheckListView<String> checkListView = multipleSelection ? new CheckListView<>(objects) : new SingleCheckListView<>(objects);

		if (!multipleSelection && initialCheckedState.size() > 1) {
			initialCheckedState = new ArrayList<Integer>(initialCheckedState.get(0));
		}
		initialCheckedState.forEach((i) -> checkListView.getCheckModel()
				.select(i));
		
		// getSelectionModel().getSelectedItems() can also have a listener

		Stage stage = new Stage();

		Button close = new Button("Close");
		VBox.setMargin(close, new Insets(5));
		close.setOnAction((e) -> {
			completeResponse(checkListView);
			stage.hide();
		});

		VBox layout = new VBox();
		layout.setAlignment(Pos.CENTER_RIGHT);
		layout.getChildren().addAll(checkListView, close);
		layout.setSpacing(5);
		layout.setPadding(new Insets(5));

		Scene scene = new Scene(layout, 400, 300);

		stage.setTitle(windowTitle);
		stage.setScene(scene);

		stage.setOnCloseRequest((e) -> {
			completeResponse(checkListView);
		});

		Platform.runLater(() -> stage.requestFocus());

		stage.initOwner(parentStage);
		stage.initModality(Modality.APPLICATION_MODAL);

		// stage.setX(parentStage.getX());
		// stage.setY(parentStage.getY());

		stage.show();
	}

	private void completeResponse(CheckListView<String> checkListView) {
		response.complete(checkListView.getCheckModel().getSelectedIndices());
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
