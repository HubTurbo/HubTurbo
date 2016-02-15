package ui.components.issue_creators;


import java.io.IOException;
import java.util.Optional;

import backend.resource.Model;
import backend.resource.TurboIssue;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.UI;

/**
 *  Abstract view of Issue Creator dialog
 */
public class IssueCreatorDialog extends Dialog<TurboIssue> {

    public static final String DEFAULT_TITLE = "Creating new issue";
    private static final String FXML_LOCATION = "fxml/IssueCreatorView.fxml";

    private final IssueCreatorPresenter presenter;

    @FXML
    private Label prompt;
    @FXML
    private VBox mainLayout;
    @FXML
    private TextField title;
    @FXML
    private TextField assigneeField;
    @FXML
    private TextField milestoneField;
    @FXML
    private FlowPane currentLabels;
    
    private IssueContentPane body;
    
    public IssueCreatorDialog(Model repo, Optional<TurboIssue> issue, Stage stage) {
        presenter = new IssueCreatorPresenter(repo, issue);
        initUI(stage);
    }

    // =================
    // UI initialisation
    // =================

    private void initUI(Stage stage) {
        initOwner(stage);
        initModality(Modality.APPLICATION_MODAL);
        loadDialogContent();
        initTitles(presenter.isNewIssue() ? DEFAULT_TITLE : presenter.resolveIssueTitle());
        initBody();
        initAssignee(presenter.getAssignee());
        initMilestone(presenter.getMilestone());
        initLabels();
        setupEvents();
    }
    
    private void loadDialogContent() {
        try {
            FXMLLoader loader = new FXMLLoader(UI.class.getResource(FXML_LOCATION));
            loader.setController(this);
            BorderPane content = (BorderPane) loader.load();
            getDialogPane().setContent(content);
        } catch (IOException e) {
            // Closes dialog when failed to load content
            close();
        }
    }

    private void setupEvents() {
        setupButtonEvent();
    }

    private void setupButtonEvent() {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        setResultConverter(button -> {
            if (button == confirmButtonType) {
                setIssueContent();
                return presenter.getResult();
            }
            return null;
        });
    }
    
    /**
     * Sets main content of issue 
     */
    private void setIssueContent() {
        presenter.setIssueTitle(title.getText());
        presenter.setIssueBody(body.getContent());
        if (!milestoneField.getText().isEmpty()) presenter.setMilestone(milestoneField.getText().trim());
        if (!assigneeField.getText().isEmpty()) presenter.setAssignee(assigneeField.getText().trim());
    }

    /**
     * Sets title for dialog window and main title
     */
    private void initTitles(String input) {
        setTitle(input);
        prompt.setText(input);
        title.setText(presenter.getIssueTitle());
        
        // Sets focus on title
        Platform.runLater(title::requestFocus);
    }
    
    private void initBody() {
        body = new IssueContentPane(presenter.getIssueBody(), presenter);
        mainLayout.getChildren().add(body);
    }

    /**
     * Sets assignee if present 
     */
    private void initAssignee(Optional<String> assignee) {
        if (assignee.isPresent()) assigneeField.setText(assignee.get());
    }

    /**
     * Sets milestone if present 
     */
    private void initMilestone(Optional<Integer> milestone) {
        if (milestone.isPresent()) milestoneField.setText(Integer.toString(milestone.get()));
    }
    
    private void initLabels() {
        presenter.getCurrentLabels().forEach(label -> currentLabels.getChildren().add(label.getNode()));
    }
}
