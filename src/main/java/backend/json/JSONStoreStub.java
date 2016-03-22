package backend.json;

import backend.resource.serialization.SerializableModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Same as JSONStore, but with the save function disabled.
 * Saves the effort of tearing down every time a test is written.
 * (explicit command line argument required for proper JSON Store to
 * activate during test mode)
 * <p>
 * The JSONStoreStub mimicks a successful write all the time, which results in a completedFuture(false).
 */
public class JSONStoreStub extends JSONStore {

    @Override
    public CompletableFuture<Boolean> saveRepository(String repoId, SerializableModel model) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public List<String> getStoredRepos() {
        return new ArrayList<>();
    }
}
