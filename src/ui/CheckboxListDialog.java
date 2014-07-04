package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Listable;

public class CheckboxListDialog implements Dialog<List<Integer>> {

	private final Stage parentStage;
	private ObservableList<String> objectNames;

	private CompletableFuture<List<Integer>> response;
	
	public CheckboxListDialog(Stage parentStage, ObservableList<Listable> objects) {
		this.parentStage = parentStage;
		ObservableList<String> stringRepresentations = FXCollections
				.observableArrayList(objects.stream()
						.map((obj) -> obj.getListName())
						.collect(Collectors.toList()));

		this.objectNames = stringRepresentations;

		response = new CompletableFuture<>();
	}

	public CompletableFuture<List<Integer>> show() {
		showDialog();
		return response;
	}

	private void showDialog() {
		
		BetterCheckListView checkListView = new BetterCheckListView(objectNames);
		initialCheckedState.forEach((i) -> checkListView.setChecked(i, true));
		
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

	private void completeResponse(BetterCheckListView checkListView) {
		response.complete(checkListView.getCheckedIndices());
	}
	
	List<Integer> initialCheckedState = new ArrayList<>();

	public List<Integer> getInitialCheckedState() {
		return initialCheckedState;
	}

	public CheckboxListDialog setInitialCheckedState(
			List<Integer> initialCheckedState) {
		this.initialCheckedState = initialCheckedState;
		return this;
	}

	String windowTitle = "";

	public String getWindowTitle() {
		return windowTitle;
	}

	public CheckboxListDialog setWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
		return this;
	}

	boolean multipleSelection = true;

	public boolean getMultipleSelection() {
		return multipleSelection;
	}

	public CheckboxListDialog setMultipleSelection(boolean multipleSelection) {
		this.multipleSelection = multipleSelection;
		return this;
	}
}
