package ui.components.issue_creators;

import java.util.Optional;

import backend.resource.Model;
import backend.resource.TurboIssue;
import javafx.application.Platform;
import javafx.stage.Stage;
import ui.UI;
import undo.actions.CreateEditIssueAction;
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

        Model repo = ui.logic.getRepo(ui.logic.getDefaultRepo());
        TurboIssue oldIssue = issue.isPresent() 
            ? issue.get() : TurboIssue.createNewIssue(repo.getRepoId());

        IssueCreatorDialog dialog = new IssueCreatorDialog(repo, oldIssue, stage);

        Optional<TurboIssue> result = dialog.showAndWait();
        stage.show();
        if (result.isPresent()) {
            TurboIssue newIssue = result.get();
            if (!newIssue.equals(oldIssue)) {
                ui.undoController.addAction(oldIssue, new CreateEditIssueAction(ui.logic, oldIssue, newIssue));
            }
        }
    }

}
