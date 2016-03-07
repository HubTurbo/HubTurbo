package ui.components.viewers;

import backend.resource.TurboIssue;
import github.ReviewComment;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.User;
import org.markdown4j.Markdown4jProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommentViewerDialog extends Dialog<Void> {

    private WebView webView;

    public CommentViewerDialog(Stage stage, TurboIssue issue) {
        // UI creation
        initialiseDialog(stage, issue);
        setupButtons();
        setupWebView();
        addCommentsToWebView(issue.getMetadata().getComments());
        getDialogPane().setContent(webView);
    }

    private void setupButtons() {
        ButtonType confirmButtonType = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType);
    }

    private void initialiseDialog(Stage stage, TurboIssue turboIssue) {
        initOwner(stage);
        initModality(Modality.NONE);
        setTitle("Viewing comments for " + (turboIssue.isPullRequest() ? "PR" : "Issue") + " #" + turboIssue.getId()
            + " in " + turboIssue.getRepoId() + " (" + turboIssue.getMetadata().getComments().size() + ")");
    }

    private String createCommentHeader(Comment comment) {
        User commenter = comment.getUser();
        String result = "<b>" + commenter.getLogin() + " (" + comment.getCreatedAt().toString() + ")</b>:";
        return result;
    }

    private void setupWebView() {
        webView = new WebView();
    }

    private void addCommentsToWebView(List<Comment> comments) {
        final StringBuilder resultingHtml = new StringBuilder();
        comments.stream().forEach(comment -> {
            resultingHtml.append(createCommentHeader(comment));
            resultingHtml.append(comment.getBodyHtml());
            resultingHtml.append("<hr>");
        });
        WebEngine webEngine = webView.getEngine();
        webEngine.loadContent(resultingHtml.toString());
    }

}
