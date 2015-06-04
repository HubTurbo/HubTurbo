package backend.json;

import backend.interfaces.RepoStore;
import backend.resource.Model;
import backend.resource.serialization.SerializableModel;

import java.util.concurrent.CompletableFuture;

/**
 * Same as JSONStore, but with the save function disabled.
 * Saves the effort of tearing down every time a test is written.
 * (explicit command line argument required for proper JSON Store to
 * activate during test mode)
 */
public class JSONStoreStub extends JSONStore {

	@Override
	public void saveRepository(String repoId, SerializableModel model) {}
}
