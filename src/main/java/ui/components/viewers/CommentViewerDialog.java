package ui.components.viewers;

import backend.resource.TurboIssue;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.eclipse.egit.github.core.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentViewerDialog extends Dialog<List<Comment>> {

    private TextArea textArea;

    CommentViewerDialog(Stage stage, TurboIssue issue) {
        // UI creation
        initialiseDialog(stage, issue);
        setupButtons();
        textArea = createTextArea();
        populateTextArea(issue.getMetadata().getComments());
        getDialogPane().setContent(textArea);
    }

    private void setupButtons() {
        ButtonType confirmButtonType = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType);

        setResultConverter((b) -> new ArrayList<Comment>());
    }

    private void initialiseDialog(Stage stage, TurboIssue turboIssue) {
        initOwner(stage);
        initModality(Modality.NONE);
        setTitle("Viewing comments for " + (turboIssue.isPullRequest() ? "PR" : "Issue") + " #" + turboIssue.getId()
            + " in " + turboIssue.getRepoId() + " (" + turboIssue.getMetadata().getComments().size() + ")");
    }

    private TextArea createTextArea() {
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setId("commentViewerTextArea");
        textArea.setPrefColumnCount(80);
        return textArea;
    }

    private void populateTextArea(List<Comment> comments) {
        comments.stream().forEach(comment -> {
            textArea.setText(textArea.getText()
                + comment.getUser().getLogin() + ": " + comment.getBodyText() + "\n\n");
        });
    }

}
