package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.controlsfx.control.textfield.TextFields;

import filter.FilterExpression;
import filter.Parser;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;

public class FilterDialog implements Dialog<FilterExpression> {

	private final Stage parentStage;
	private final Model logic;

	private final CompletableFuture<FilterExpression> response;

	public FilterDialog(Stage parentStage, Model logic) {
		this.parentStage = parentStage;
		this.logic = logic;
		
		response = new CompletableFuture<>();
	}

	public CompletableFuture<FilterExpression> show() {
		showDialog();
		return response;
	}

	private Node createRoot(Stage stage) {
        TextField field = new TextField("title: one");
        HBox.setHgrow(field, Priority.ALWAYS);
        setupAutocompletion(field);
                 
        Button close = new Button("Close");
        close.setOnAction((e) -> {
        	response.complete(Parser.parse(field.getText()));
        	stage.close();
        });
        
        VBox layout = new VBox();
        layout.getChildren().addAll(field, close);
        layout.setAlignment(Pos.CENTER_RIGHT);
        layout.setSpacing(10);
        HBox.setHgrow(layout, Priority.ALWAYS);
        
        return layout;
	}

	private void setupAutocompletion(TextField field) {
        ArrayList<String> words = new ArrayList<String>();
        words.addAll(logic.getIssues().stream().map((x) -> x.getListName()).collect(Collectors.toList()));
        words.addAll(logic.getLabels().stream().map((x) -> x.getListName()).collect(Collectors.toList()));
        words.addAll(logic.getMilestones().stream().map((x) -> x.getListName()).collect(Collectors.toList()));
        words.addAll(logic.getCollaborators().stream().map((x) -> x.getListName()).collect(Collectors.toList()));
        addSyntax(words);
        
        TextFields.bindAutoCompletion(field, words.toArray());
	}

	private void addSyntax(ArrayList<String> keywords) {
		keywords.addAll(Arrays.asList(new String[] {
				"milestone:",
				"labels:",
				"assignee:",
				"title:",
		}));
		for (String keyword : new ArrayList<String>(keywords)) {
			keywords.add("-" + keyword);
		}
	}

	private void showDialog() {

		HBox layout = new HBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);

		Scene scene = new Scene(layout, 400, 100);

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
