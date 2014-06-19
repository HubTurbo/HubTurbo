package ui;

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
import javafx.stage.Stage;
import logic.TurboIssue;
import logic.Listable;

public class FilterableCheckboxList {

	// private static final double HEIGHT_FACTOR = 0.3;
	//
	// private static final int TITLE_SPACING = 5;
	// private static final int ELEMENT_SPACING = 10;
	// private static final int MIDDLE_SPACING = 20;
	//
	// public static final String STYLE_YELLOW =
	// "-fx-background-color: #FFFA73;";
	// public static final String STYLE_BORDERS =
	// "-fx-border-color: #000000; -fx-border-width: 1px;";

	Stage parentStage;
	// TurboIssue issue;
	FilteredList<String> objects;

	CompletableFuture<String> response;

	public FilterableCheckboxList(Stage parentStage, ObservableList<Listable> objects) {
		this.parentStage = parentStage;
		ObservableList<String> stringRepresentations = FXCollections.observableArrayList(objects.stream().map((obj) -> obj.getListName()).collect(Collectors.toList()));
		this.objects = new FilteredList<>(stringRepresentations, p -> true);;

		response = new CompletableFuture<>();
	}

	public CompletableFuture<String> show() {
		showDialog();
		return response;
	}

	private void showDialog() {

		VBox layout = new VBox();

		TextField searchField = new TextField();
		searchField.setPromptText("Search");

		CheckListView<String> checkListView = new CheckListView<>(objects);
		checkListView.getSelectionModel().setSelectionMode(
				SelectionMode.MULTIPLE);

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

		// layout.setPadding(new Insets(15));
		// layout.setSpacing(MIDDLE_SPACING);

		layout.getChildren().addAll(searchField, checkListView);

		Scene scene = new Scene(layout, 400, 300);

		Stage stage = new Stage();
		// stage.setTitle("Select");
		stage.setScene(scene);

		Platform.runLater(() -> stage.requestFocus());

		// layout.getChildren().addAll();

		stage.initOwner(parentStage);
		// secondStage.initModality(Modality.APPLICATION_MODAL);

		// stage.setX(parentStage.getX());
		// stage.setY(parentStage.getY() + parentStage.getHeight() * (1 -
		// HEIGHT_FACTOR));

		stage.show();
	}
}
