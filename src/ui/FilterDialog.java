package ui;

import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;

public class FilterDialog implements Dialog<String> {
	
	private final Stage parentStage;
//	private final Model logic;

	private final CompletableFuture<String> response;
	private String input = "";
	
	public FilterDialog(Stage parentStage, Model logic, String input) {
		this.parentStage = parentStage;
//		this.logic = logic;
		this.input = input;

		response = new CompletableFuture<>();
	}

	public CompletableFuture<String> show() {
		showDialog();
		return response;
	}

	private Node createRoot(Stage stage) {
		
		Label explanatory = new Label("Filter issues by writing a series of predicates.\n\ne.g. \"all issues assigned to John that aren't closed and are due in milestones v0.1 and v0.2\"\n\nassignee(john) ~status(closed) (milestone(v0.1) or milestone(v0.2))");
		explanatory.setWrapText(true);
		
        TextField field = new TextField(input);
        HBox.setHgrow(field, Priority.ALWAYS);
//        setupAutocompletion(field);

        HBox buttonContainer = new HBox();
        Button close = new Button("Filter");
        close.setOnAction((e) -> {
        	response.complete(field.getText());
        	stage.close();
        });
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.getChildren().add(close);
        
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setSpacing(10);
        HBox.setHgrow(layout, Priority.ALWAYS);
        layout.getChildren().addAll(explanatory, field, buttonContainer);
        
        return layout;
	}

//	private void setupAutocompletion(TextField field) {
//        ArrayList<String> words = new ArrayList<String>();
//        words.addAll(logic.getIssues().stream().map((x) -> x.getListName()).collect(Collectors.toList()));
//        words.addAll(logic.getLabels().stream().map((x) -> x.getListName()).collect(Collectors.toList()));
//        words.addAll(logic.getMilestones().stream().map((x) -> x.getListName()).collect(Collectors.toList()));
//        words.addAll(logic.getCollaborators().stream().map((x) -> x.getListName()).collect(Collectors.toList()));
//        addSyntax(words);
//        
//        TextFields.bindAutoCompletion(field, words.toArray());
//	}

//	private void addSyntax(ArrayList<String> keywords) {
//		keywords.addAll(Arrays.asList(new String[] {
//				"milestone:",
//				"labels:",
//				"assignee:",
//				"title:",
//		}));
//		for (String keyword : new ArrayList<String>(keywords)) {
//			keywords.add("-" + keyword);
//		}
//	}

	private void showDialog() {

		HBox layout = new HBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);

		Scene scene = new Scene(layout, 530, 200);

		Stage stage = new Stage();
		stage.setTitle("Filter");
		stage.setScene(scene);

		Platform.runLater(() -> stage.requestFocus());

		layout.getChildren().addAll(createRoot(stage));

		stage.initOwner(parentStage);
		// secondStage.initModality(Modality.APPLICATION_MODAL);

		stage.setX(parentStage.getX());
		stage.setY(parentStage.getY());

		stage.show();
	}
}
