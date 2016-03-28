package backend.json;

import backend.interfaces.RepoStore;
import backend.resource.Model;
import backend.resource.serialization.SerializableModel;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;
import util.HTLog;
import util.Utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.google.common.io.Files.getFileExtension;

public class JSONStore extends RepoStore {

    private static final Logger logger = HTLog.get(JSONStore.class);

    @Override
    public CompletableFuture<Model> loadRepository(String repoId) {
        CompletableFuture<Model> response = new CompletableFuture<>();
        addTask(new ReadTask(repoId, response));
        return response;
    }

    @Override
    public CompletableFuture<Boolean> saveRepository(String repoId, SerializableModel model) {
        CompletableFuture<Boolean> response = new CompletableFuture<>();
        addTask(new WriteTask(repoId, model, response));
        return response;
    }

    public List<String> getStoredRepos() {
        ensureDirectoryExists();
        try {
            return Files.walk(Paths.get(RepoStore.directory), 1)
                    .filter(Files::isRegularFile)
                    .filter(p -> getFileExtension(String.valueOf(p.getFileName())).equalsIgnoreCase("json"))
                    .map(JSONStore::getRepositoryIdFromJson)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Unable to open stored repository directory. ");
            return new ArrayList<>();
        }
    }

    public CompletableFuture<Boolean> removeStoredRepo(String repoId) {
        CompletableFuture<Boolean> response = new CompletableFuture<>();
        addTask(new DeleteTask(repoId, response));
        return response;
    }

    private static Optional<String> getRepositoryIdFromJson(Path p) {
        try {
            String repoId = new Model(
                    (SerializableModel) new Gson().fromJson(Utility.readFile(String.valueOf(p.toAbsolutePath())).get(),
                                                            new TypeToken<SerializableModel>() {}.getType()))
                    .getRepoId();
            if (String.valueOf(p.getFileName()).equalsIgnoreCase(escapeRepoName(repoId))) {
                logger.info("Adding " + p.getFileName() + " to stored repository list. ");
                return Optional.of(repoId);
            }
        } catch (NullPointerException | JsonParseException e) {
            logger.error("Unable to load repository from " + p.getFileName());
        }
        return Optional.empty();
    }
}
