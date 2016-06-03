package ui.components.pickers;

import backend.interfaces.Repo;
import javafx.application.Platform;
import javafx.stage.Stage;
import prefs.RepoInfo;
import ui.UI;
import util.events.ShowRepositoryPickerEventHandler;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * This class represents an entry point to RepositoryPicker by handling operations related to RepositoryPickerDialog.
 */
public class RepositoryPicker {

    private final UI ui;
    private final Consumer<String> onValueChangeCallback;

    public RepositoryPicker(UI ui, Consumer<String> onValueChangeCallback) {
        this.ui = ui;
        this.onValueChangeCallback = onValueChangeCallback;
        ui.registerEvent((ShowRepositoryPickerEventHandler) e -> Platform.runLater(() -> showRepositoryPicker()));
    }

    private void showRepositoryPicker() {
        Set<String> storedRepos1 = ui.logic.getStoredRepos();
        // should also have a set of aliased repos
        // then show all the stored repos
        // but for repos with aliases, show the alias instead
        // scratch that
        // should show a set of repos that is taken from Repos
        List<RepoInfo> storedRepos = UI.prefs.getRepos();
        new RepositoryPickerDialog(storedRepos, this::pickRepository, (repoId) -> ui.logic.isRepositoryValid(repoId));
    }

    private void pickRepository(Optional<String> repoId) {
        String defaultRepo = ui.logic.getDefaultRepo();
        if (!repoId.isPresent()) {
            return;
        }
        if (!defaultRepo.equals(repoId.get())) {
            onValueChangeCallback.accept(repoId.get());
        }
    }

}
