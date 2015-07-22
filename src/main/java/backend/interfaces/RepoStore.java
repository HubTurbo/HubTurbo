package backend.interfaces;

import backend.resource.Model;
import backend.resource.serialization.SerializableModel;
import util.Utility;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class RepoStore {

    protected static String directory = "store";
    public static final String TEST_DIRECTORY = "store/test";
    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    public static String escapeRepoName(String repoName) {
        return repoName.replace("/", "-") + ".json";
    }

    protected void addTask(StoreTask task) {
        pool.execute(task);
    }

    public abstract CompletableFuture<Model> loadRepository(String repoId);
    public abstract void saveRepository(String repoId, SerializableModel model);

    private static String getRepoPath(String repoId) {
        ensureDirectoryExists();
        String newRepoName = RepoStore.escapeRepoName(repoId);
        return new File(RepoStore.directory, newRepoName).getAbsolutePath();
    }

    public static void write(String repoId, String output, int issueCount) {
        Utility.writeFile(getRepoPath(repoId), output, issueCount);
    }

    public static Optional<String> read(String repoId) {
        return Utility.readFile(getRepoPath(repoId));
    }

    protected static void ensureDirectoryExists() {
        File directory = new File(RepoStore.directory);
        if (!directory.exists() || !directory.isDirectory()) {
            directory.mkdirs();
        }
    }

    public static void enableTestDirectory() {
        changeDirectory(RepoStore.TEST_DIRECTORY);
    }

    private static void changeDirectory(String newDir) {
        RepoStore.directory = newDir;
    }

}
