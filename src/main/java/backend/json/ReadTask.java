package backend.json;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.Logger;

import util.HTLog;
import util.exceptions.JSONLoadException;
import util.exceptions.RepoStoreException;
import backend.interfaces.RepoStore;
import backend.interfaces.StoreTask;
import backend.resource.Model;
import backend.resource.serialization.SerializableModel;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

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
            logger.error(HTLog.format(repoId, "Failed to load from store"));
            response.completeExceptionally(e);
        }
    }

    /**
     * Load repository data from RepoStore into a new Model.
     * @param repoId the string id of the repository to be loaded
     * @return a new Model containing data for the requested repository.
     * @throws JSONLoadException
     */
    private Model load(String repoId) throws RepoStoreException {
        Optional<String> input = RepoStore.read(repoId);

        if (!input.isPresent()) {
            logger.error("Unable to load " + repoId + " from JSON cache; defaulting to an empty Model");
            throw new JSONLoadException();
        } else {
            logger.info(HTLog.format(repoId, "Loaded from JSON cache"));

            try {
                SerializableModel sModel = new Gson().fromJson(input.get(),
                        new TypeToken<SerializableModel>(){}.getType());

                return new Model(sModel);
            } catch (JsonParseException e) {
                logger.error(HTLog.format(repoId, "JSON data is corrupted"));
                throw new JSONLoadException();
            }
        }
    }
}

