package ui.components.issue_creators;

import java.util.Optional;

import backend.resource.TurboIssue;
import javafx.application.Platform;
import javafx.stage.Stage;
import ui.UI;
import util.events.ShowIssueCreatorEventHandler;

/**
 * Allows user to create or edit issues
 *
 */
public class IssueCreator {

    private final UI ui;
    private final Stage stage;

    public IssueCreator(UI ui, Stage stage) {
        this.ui = ui;
        this.stage = stage;
        ui.registerEvent((ShowIssueCreatorEventHandler) e -> Platform.runLater(
                () -> showIssueCreator(e.issue)));
    }

    private void showIssueCreator(Optional<TurboIssue> issue) {

        IssueCreatorDialog dialog = new IssueCreatorDialog(ui.logic.getRepo(ui.logic.getDefaultRepo()), issue, stage);

        Optional<TurboIssue> result = dialog.showAndWait();
        stage.show();
        if (result.isPresent()) {
            ui.logic.createIssue(result.get().getRepoId(), result.get());
        }
    }

}
