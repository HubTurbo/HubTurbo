package backend;

import static util.Futures.withResult;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import github.TurboIssueEvent;
import org.apache.logging.log4j.Logger;

import backend.resource.Model;
import backend.resource.MultiModel;
import org.eclipse.egit.github.core.Comment;
import prefs.Preferences;
import ui.UI;
import util.Futures;
import util.HTLog;
import util.Utility;
import util.events.testevents.ClearLogicModelEventHandler;
import util.events.RepoOpenedEvent;

public class Logic {

    private static final Logger logger = HTLog.get(Logic.class);

    private final MultiModel models;
    private final UIManager uiManager;
    private final Preferences prefs;

    private RepoIO repoIO;

    // Assumed to be always present when app starts
    public UserCredentials credentials = null;

    public Logic(UIManager uiManager, Preferences prefs, boolean isTestMode, boolean enableTestJSON) {
        this.uiManager = uiManager;
        this.prefs = prefs;
        this.models = new MultiModel(prefs);

        repoIO = new RepoIO(isTestMode, enableTestJSON);

        // Only relevant to testing, need a different event type to avoid race condition
        UI.events.registerEvent((ClearLogicModelEventHandler) e -> {
            // DELETE_* and RESET_REPO is handled jointly by Logic and DummyRepo
            assert isTestMode;
            assert e.repoId != null;

            List<Model> toReplace = models.toModels();

            logger.info("Attempting to reset " + e.repoId);
            if (toReplace.remove(models.get(e.repoId))) {
                logger.info("Clearing " + e.repoId + " successful.");
            } else {
                logger.info(e.repoId + " not currently in model.");
            }
            models.replace(toReplace);

            // Re-"download" repo after clearing
            openRepository(e.repoId);
        });

        // Pass the currently-empty model to the UI
        uiManager.updateNow(models);
    }

    private CompletableFuture<Boolean> isRepositoryValid(String repoId) {
        return repoIO.isRepositoryValid(repoId);
    }

    public CompletableFuture<Boolean> login(String username, String password) {
        String message = "Logging in as " + username;
        logger.info(message);
        UI.status.displayMessage(message);

        credentials = new UserCredentials(username, password);
        return repoIO.login(credentials);
    }

    public void refresh() {
        String message = "Refreshing " + models.toModels().stream()
            .map(Model::getRepoId)
            .collect(Collectors.joining(", "));

        logger.info(message);
        UI.status.displayMessage(message);

        Futures.sequence(models.toModels().stream()
            .map(repoIO::updateModel)
            .collect(Collectors.toList()))
                .thenApply(models::replace)
                .thenRun(this::updateUI)
                .exceptionally(Futures::log);
    }

    public CompletableFuture<Boolean> openRepository(String repoId) {
        assert Utility.isWellFormedRepoId(repoId);
        if ((isAlreadyOpen(repoId) && repoId.equals(getDefaultRepo())) ||
                models.isRepositoryPending(repoId)) {
            return Futures.unit(false);
        }
        models.queuePendingRepository(repoId);
        return isRepositoryValid(repoId).thenCompose(valid -> {
            if (!valid) {
                return Futures.unit(false);
            } else {
                prefs.addToLastViewedRepositories(repoId);
                logger.info("Opening " + repoId);
                UI.status.displayMessage("Opening " + repoId);
                return repoIO.openRepository(repoId)
                    .thenApply(models::addPending)
                    .thenRun(this::updateUI)
                    .thenRun(() -> UI.events.triggerEvent(new RepoOpenedEvent(repoId)))
                    .thenApply(n -> true)
                    .exceptionally(withResult(false));
            }
        });
    }

    public CompletableFuture<Map<Integer, IssueMetadata>> getIssueMetadata(String repoId, List<Integer> issues) {
        String message = "Getting metadata for " + repoId + "...";
        logger.info("Getting metadata for issues " + issues);
        UI.status.displayMessage(message);

        String currentUser = prefs.getLastLoginUsername();

        return repoIO.getIssueMetadata(repoId, issues).thenApply(this::processNonSelfUpdate)
            .thenApply(metadata -> {
                String updatedMessage = "Received metadata from " + repoId + "!";
                UI.status.displayMessage(updatedMessage);
                models.insertMetadata(repoId, metadata, currentUser);
                return metadata;
            }).thenApply(Futures.tap(this::updateUIWithMetadata))
            .exceptionally(withResult(new HashMap<>()));
    }

    // Adds update times to the metadata map
    private Map<Integer, IssueMetadata> processNonSelfUpdate(Map<Integer, IssueMetadata> metadata) {
        String currentUser = prefs.getLastLoginUsername();

        // Iterates through each entry in the metadata set, and looks for the comment/event with
        // the latest time created.
        for (Map.Entry<Integer, IssueMetadata> entry : metadata.entrySet()) {
            IssueMetadata currentMetadata = entry.getValue();
            Date lastNonSelfUpdate = new Date(0);
            for (TurboIssueEvent event : currentMetadata.getEvents()) {
                if (!event.getActor().getLogin().equalsIgnoreCase(currentUser)
                        && event.getDate().after(lastNonSelfUpdate)) {
                    lastNonSelfUpdate = event.getDate();
                }
            }
            for (Comment comment : currentMetadata.getComments()) {
                if (!comment.getUser().getLogin().equalsIgnoreCase(currentUser)
                        && comment.getCreatedAt().after(lastNonSelfUpdate)) {
                    lastNonSelfUpdate = comment.getCreatedAt();
                }
            }

            entry.setValue(new IssueMetadata(currentMetadata,
                    LocalDateTime.ofInstant(lastNonSelfUpdate.toInstant(), ZoneId.systemDefault()),
                    calculateNonSelfCommentCount(currentMetadata.getComments(), currentUser)
            ));
        }
        return metadata;
    }

    private int calculateNonSelfCommentCount(List<Comment> comments, String currentUser) {
        int result = 0;
        for (Comment comment : comments) {
            if (!comment.getUser().getLogin().equalsIgnoreCase(currentUser)) {
                result++;
            }
        }
        return result;
    }

    public Set<String> getOpenRepositories() {
        return models.toModels().stream()
            .map(Model::getRepoId)
            .collect(Collectors.toSet());
    }

    public boolean isAlreadyOpen(String repoId) {
        return getOpenRepositories().contains(repoId);
    }

    public void setDefaultRepo(String repoId) {
        models.setDefaultRepo(repoId);
    }

    public String getDefaultRepo() {
        return models.getDefaultRepo();
    }

    private void updateUI() {
        uiManager.update(models, false);
    }

    private void updateUIWithMetadata() {
        uiManager.update(models, true);
    }
}

