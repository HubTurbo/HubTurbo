package backend.json;

import backend.interfaces.RepoStore;
import backend.interfaces.StoreTask;
import backend.resource.Model;
import backend.resource.serialization.SerializableModel;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Logger;
import util.HTLog;
import util.exceptions.JSONLoadException;
import util.exceptions.RepoStoreException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class ReadTask extends StoreTask {

    private static final Logger logger = HTLog.get(ReadTask.class);

    public final CompletableFuture<Model> response;

    public ReadTask(String repoId, CompletableFuture<Model> response) {
        super(repoId);
        this.response = response;
    }

    @Override
    public void run() {
        try {
            Model model = load(repoId);
            response.complete(model);
        } catch (RepoStoreException e) {
            logger.error(HTLog.format(repoId, "Unable to load from store"));
            response.completeExceptionally(e);
        }
    }

    /**
     * Loads repository data from RepoStore into a new Model.
     *
     * @param repoId the string id of the repository to be loaded
     * @return a new Model containing data for the requested repository.
     * @throws JSONLoadException when the repository's JSON data cannot be
     *                           retrieved from the local store or is corrupted
     */
    private Model load(String repoId) throws RepoStoreException {
        Optional<String> input = RepoStore.read(repoId);

        if (!input.isPresent()) {
            logger.error("Unable to load " + repoId + " from JSON cache");
            throw new JSONLoadException();
        } else {
            logger.info(HTLog.format(repoId, "Data loaded from JSON cache"));

            try {
                SerializableModel sModel = new Gson().fromJson(input.get(),
                                                               new TypeToken<SerializableModel>() {}.getType());

                return new Model(sModel);
            } catch (NullPointerException | JsonParseException e) {
                logger.error(HTLog.format(repoId, "JSON data is corrupted"));
                throw new JSONLoadException(e);
            }
        }
    }
}

