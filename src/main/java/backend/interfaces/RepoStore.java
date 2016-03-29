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

    public abstract CompletableFuture<Boolean> saveRepository(String repoId, SerializableModel model);

    private static Optional<String> getRepoPath(String repoId) {
        if (ensureDirectoryExists()) {
            String newRepoName = RepoStore.escapeRepoName(repoId);
            return Optional.of(new File(RepoStore.directory, newRepoName).getAbsolutePath());
        }
        return Optional.empty();
    }

    public static boolean write(String repoId, String output, int issueCount) {
        return Utility.writeFile(getRepoPath(repoId).orElse(""), output, issueCount);
    }

    public static Optional<String> read(String repoId) {
        return Utility.readFile(getRepoPath(repoId).orElse(""));
    }

    public static boolean delete(String repoId) {
        return Utility.deleteFile(getRepoPath(repoId).orElse(""));
    }

    /**
     * Returns true on success.
     *
     * @return
     */
    protected static boolean ensureDirectoryExists() {
        File directory = new File(RepoStore.directory);
        boolean directoryNonExistent = !directory.exists() || !directory.isDirectory();
        if (directoryNonExistent) {
            return directory.mkdirs();
        }
        return true;
    }

    public static void changeDirectory(String newDir) {
        RepoStore.directory = newDir;
    }

}
