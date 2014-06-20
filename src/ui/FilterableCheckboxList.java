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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.TurboIssue;
import logic.Listable;

public class FilterableCheckboxList {

	Stage parentStage;
	FilteredList<String> objects;

	CompletableFuture<List<Integer>> response;

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
		checkListView.getSelectionModel().setSelectionMode(
				multipleSelection ? SelectionMode.MULTIPLE
						: SelectionMode.SINGLE);

		initialCheckedState.forEach((i) -> checkListView.getCheckModel()
				.select(i));

		// checkListView.getSelectionModel().getSelectedItems()
		// .addListener(new ListChangeListener<Listable>() {
		// @Override
		// public void onChanged(
		// ListChangeListener.Change<? extends Listable> c) {
		// // updateText(selectedItemsLabel, c.getList());
		// System.out.println("selection changed");
		// }
		// });
		// checkListView.getCheckModel().getSelectedItems()
		// .addListener(new ListChangeListener<Listable>() {
		// @Override
		// public void onChanged(
		// ListChangeListener.Change<? extends Listable> c) {
		//
		// System.out.println("check state changed");
		// }
		// });

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
