package ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.controlsfx.control.CheckListView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.TurboLabel;

public class LabelCheckboxListDialog implements Dialog<List<TurboLabel>> {

	private Stage parentStage;
	private CompletableFuture<List<TurboLabel>> response;

	private ObservableList<TurboLabel> labels;
	private ArrayList<CheckListView<String>> controls = new ArrayList<>();
	
	public LabelCheckboxListDialog(Stage parentStage, ObservableList<TurboLabel> labels) {
		this.labels = labels;
		this.parentStage = parentStage;
		
		response = new CompletableFuture<>();
	}

	@Override
	public CompletableFuture<List<TurboLabel>> show() {
		showDialog();
		return response;
	}

	private void showDialog() {

//		CheckListView<String> checkListView = new SingleCheckListView<>();
		
		HashMap<String, ArrayList<TurboLabel>> groups = ManageLabelsDialog.groupLabels(labels);
				
//		layout.setAlignment(Pos.CENTER_RIGHT);
//		layout.setSpacing(5);
//		layout.setPadding(new Insets(5));


		VBox layout = new VBox();
		for (String groupName : groups.keySet()) {
			List<String> labelNames = groups.get(groupName).stream().map(l -> l.getValue()).collect(Collectors.toList());
			
			boolean isExclusive = new TurboLabelGroup(groups.get(groupName)).isExclusive();
			
			CheckListView<String> control =
					isExclusive ? new SingleCheckListView<String>(FXCollections.observableArrayList(labelNames))
					: new CheckListView<>(FXCollections.observableArrayList(labelNames));

			control.setUserData(groups.get(groupName));
			controls.add(control);
			
			// TODO add some kind of title label
			layout.getChildren().add(control);
		}
//		layout.getChildren().addAll(checkListView);
		
		setupLayout(layout);
	}

	private void setupLayout(VBox layout) {
		Stage stage = new Stage();

		Button close = new Button("Close");
		VBox.setMargin(close, new Insets(5));
		close.setOnAction((e) -> {
			completeResponse();
			stage.hide();
		});
		
		layout.setAlignment(Pos.CENTER_RIGHT);
		layout.setSpacing(5);
		layout.setPadding(new Insets(5));
		layout.getChildren().add(close);

		Scene scene = new Scene(layout, 400, 300);

		stage.setTitle("Choose Labels");
		stage.setScene(scene);

		stage.setOnCloseRequest((e) -> {
			completeResponse();
		});

		Platform.runLater(() -> stage.requestFocus());

		stage.initOwner(parentStage);
		stage.initModality(Modality.APPLICATION_MODAL);

		// stage.setX(parentStage.getX());
		// stage.setY(parentStage.getY());

		stage.show();
	}

	private void completeResponse() {
		ArrayList<TurboLabel> result = new ArrayList<>();
		for (CheckListView<String> clv : controls) {
			@SuppressWarnings("unchecked")
			ArrayList<TurboLabel> labels = ((ArrayList<TurboLabel>) clv.getUserData());
			result.addAll(clv.getCheckModel().getSelectedIndices().stream().map(i -> labels.get(i)).collect(Collectors.toList()));
		}
		response.complete(result);
	}
}
