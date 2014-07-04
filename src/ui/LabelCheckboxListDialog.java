package ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.controlsfx.control.CheckListView;
import org.hamcrest.core.IsEqual;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.TurboLabel;

public class LabelCheckboxListDialog implements Dialog<List<TurboLabel>> {

	private static final double WINDOW_WIDTH = 250;
	private static final double WINDOW_HEIGHT = 370;
	
	private static final int ROW_HEIGHT = 30;
	
	private Stage parentStage;
	private CompletableFuture<List<TurboLabel>> response;
	private ArrayList<TurboLabel> initialChecked;

	private ObservableList<TurboLabel> labels;
	private ArrayList<Node> controls = new ArrayList<>();
	
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

	@SuppressWarnings("unchecked")
	private void showDialog() {
		
		HashMap<String, ArrayList<TurboLabel>> groups = ManageLabelsDialog.groupLabels(labels);

		VBox layout = new VBox();
		layout.setSpacing(4);
		
		for (String groupName : groups.keySet()) {
			List<String> labelNames = groups.get(groupName).stream().map(l -> l.getValue()).collect(Collectors.toList());
			
			boolean isExclusive = new TurboLabelGroup(groups.get(groupName)).isExclusive();
			
			Node control;
			if (isExclusive) {
				control = new SingleCheckListView(FXCollections.observableArrayList(labelNames));

				((SingleCheckListView) control).setPrefHeight(labelNames.size() * ROW_HEIGHT + 2);
				((SingleCheckListView) control).setPrefWidth(WINDOW_WIDTH - 29);
			}
			else {
				control = new CheckListView<String>(FXCollections.observableArrayList(labelNames));

				((CheckListView<String>) control).setPrefHeight(labelNames.size() * ROW_HEIGHT + 2);
				((CheckListView<String>) control).setPrefWidth(WINDOW_WIDTH - 29);
				
			}
			control.setUserData(groups.get(groupName));
			controls.add(control);	

			// deal with initially checked items
			int selected = 0;
			for (int i=0; i<groups.get(groupName).size(); i++) {
				if (initialChecked.contains(groups.get(groupName).get(i))) {
					if (isExclusive) {
//						control.getCheckModel().select(i);
					}
					else {
						((CheckListView<String>) control).getCheckModel().select(i);
					}
					selected++;
				}
			}
			assert !isExclusive || selected == 1 || selected == 0;
			
			// layout
			Label name = new Label(groupName);
			layout.getChildren().addAll(name, control);
		}

		ScrollPane sp = new ScrollPane();
		sp.setHbarPolicy(ScrollBarPolicy.NEVER);
		sp.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		sp.setContent(layout);
		VBox.setVgrow(sp, Priority.ALWAYS);
		setupLayout(sp);
	}

	private void setupLayout(ScrollPane content) {
		Stage stage = new Stage();

		Button close = new Button("Close");
		VBox.setMargin(close, new Insets(5));
		close.setOnAction((e) -> {
			completeResponse();
			stage.hide();
		});
		
		VBox layout = new VBox();
		layout.setAlignment(Pos.CENTER_RIGHT);
		layout.setSpacing(5);
		layout.setPadding(new Insets(5));
		layout.getChildren().addAll(content, close);

		Scene scene = new Scene(layout, WINDOW_WIDTH, WINDOW_HEIGHT);

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

	@SuppressWarnings("unchecked")
	private void completeResponse() {
		ArrayList<TurboLabel> result = new ArrayList<>();
		for (Node clv : controls) {
			ArrayList<TurboLabel> labels = ((ArrayList<TurboLabel>) clv.getUserData());
			boolean isExclusive = new TurboLabelGroup(labels).isExclusive();

			if (isExclusive) {

			} else {
				result.addAll(((CheckListView<String>) clv).getCheckModel().getSelectedIndices().stream().map(i -> labels.get(i)).collect(Collectors.toList()));
			}
			
		}
		response.complete(result);
	}

	public LabelCheckboxListDialog setInitialChecked(List<TurboLabel> initialChecked) {
		this.initialChecked = new ArrayList<>(initialChecked);
		return this;
	}
}
