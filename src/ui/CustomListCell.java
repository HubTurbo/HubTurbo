package ui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;
import model.TurboUser;

public class CustomListCell extends ListCell<TurboIssue> {

	private static final String STYLE_ISSUE_NAME = "-fx-font-size: 24px;";

	private final Stage mainStage;
	private final Model model;
	
	public CustomListCell(Stage mainStage, Model model, IssuePanel parent) {
		super();
		this.mainStage = mainStage;
		this.model = model;
		Font.loadFont(getClass().getResource("octicons-local.ttf").toExternalForm(), 24);

	}

	@Override
	public void updateItem(TurboIssue issue, boolean empty) {
		super.updateItem(issue, empty);
		if (issue == null)
			return;
		
		HBox buttonBox = new HBox();
		Label ghIcon = new Label();
		ghIcon.setStyle("-fx-font-family: github-octicons; -fx-font-size: 22px; -fx-background-color: transparent; -fx-padding: 0 5 0 0;");
		ghIcon.setText("G");
		buttonBox.getChildren().addAll(ghIcon);
		buttonBox.setOnMouseClicked((MouseEvent e) -> {
			if (issue.getHtmlUrl() != null) {
				try {
			        Desktop desktop = Desktop.getDesktop();
			        if (Desktop.isDesktopSupported()
			                && desktop.isSupported(Desktop.Action.BROWSE)) {
			            URI uri = new URI(issue.getHtmlUrl());
			            desktop.browse(uri);
			        }
			    } catch (IOException ex) {
			        ex.printStackTrace();
			    } catch (URISyntaxException ex) {
			        ex.printStackTrace();
			    }	
			}
		});
		
		Text issueName = new Text("#" + issue.getId() + " " + issue.getTitle());
		issueName.setStyle(STYLE_ISSUE_NAME);
		issue.titleProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				issueName.setText("#" + issue.getId() + " " + newValue);
			}
		});
		
		HBox titleBox = new HBox();
		titleBox.getChildren().addAll(buttonBox, issueName);

		ParentIssuesDisplayBox parents = new ParentIssuesDisplayBox(issue.getParents(), false);
		parents.setOnMouseClicked((MouseEvent e) -> {
			System.out.println("Parents clicked!");
		});
		
		LabelDisplayBox labels = new LabelDisplayBox(issue.getLabels(), false);

		HBox assignee = new HBox();
		assignee.setSpacing(3);
		Text assignedToLabel = new Text("Assigned to:");
		TurboUser collaborator = issue.getAssignee();
		Text assigneeName = new Text(collaborator == null ? "none"
				: collaborator.getGithubName());
		assignee.getChildren().addAll(assignedToLabel, assigneeName);

		VBox everything = new VBox();
		everything.setSpacing(2);
		everything.getChildren()
				.addAll(titleBox, parents, labels, assignee);
		// everything.getChildren().stream().forEach((node) ->
		// node.setStyle(Demo.STYLE_BORDERS));

		setGraphic(everything);

		setStyle(UI.STYLE_BORDERS + "-fx-border-radius: 5;");

		registerEvents(issue);
	}

	private void registerEvents(TurboIssue issue) {
		setOnMouseClicked((MouseEvent mouseEvent) -> {
			if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
				if (mouseEvent.getClickCount() == 2) {
					onDoubleClick(issue);
				}
			}
		});
	}

	private void onDoubleClick(TurboIssue issue) {
		TurboIssue modifiedIssue = new TurboIssue(issue);
		(new IssueDialog(mainStage, model, modifiedIssue)).show().thenApply(
				response -> {
					if (response.equals("ok")) {
						model.updateIssue(issue, modifiedIssue);
					}
					return true;
				});
	}
}
