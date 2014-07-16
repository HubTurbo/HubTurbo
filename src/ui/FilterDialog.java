package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FilterDialog extends Dialog<String> {
	
	private String input = "";
	
	public FilterDialog(Stage parentStage, String input) {
		super(parentStage);
		this.input = input;
	}

	private Node createRoot() {
		
		Label explanatory = new Label("Filter issues by writing a series of predicates.\n\ne.g. \"all issues assigned to John that aren't closed and are due in milestones v0.1 and v0.2\"\n\nassignee(john) ~status(closed) (milestone(v0.1) or milestone(v0.2))\nassignee:john -status:closed (milestone:v0.1 or milestone:v0.2)");
		explanatory.setWrapText(true);
		
        TextField field = new TextField(input);
        HBox.setHgrow(field, Priority.ALWAYS);
//        setupAutocompletion(field);
        
        field.setOnAction(ke -> {
        	completeResponse(field.getText());
            close();
        });

        HBox buttonContainer = new HBox();
        Button close = new Button("Apply");
        close.setOnAction((e) -> {
        	completeResponse(field.getText());
        	close();
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

	protected Parent content() {

		HBox layout = new HBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		layout.getChildren().addAll(createRoot());
		setSize(530, 220);
		setTitle("Filter");
		return layout;
	}
}
