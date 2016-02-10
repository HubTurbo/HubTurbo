package ui.components.viewers;

import backend.resource.TurboIssue;
import javafx.application.Platform;
import javafx.stage.Stage;
import ui.UI;
import util.events.ShowCommentViewerEventHandler;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class CommentViewer {
    UI ui;
    Stage stage;

    public CommentViewer(UI ui, Stage stage) {
        this.ui = ui;
        this.stage = stage;
        ui.registerEvent((ShowCommentViewerEventHandler) e -> Platform.runLater(() -> showCommentViewerDialog(e.issue)));
    }

    private void showCommentViewerDialog(TurboIssue issue) {
        ui.logic.getIssueMetadata(issue.getRepoId(), Arrays.asList(issue))
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        CommentViewerDialog dialog = new CommentViewerDialog(stage, issue);
                        dialog.setHeight(500);
                        dialog.setResizable(true);
                        dialog.showAndWait();
                    });
                });
    }

}
