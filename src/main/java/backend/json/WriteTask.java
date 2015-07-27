package backend.json;

import backend.interfaces.RepoStore;
import backend.interfaces.StoreTask;
import backend.resource.serialization.SerializableModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.concurrent.CompletableFuture;

class WriteTask extends StoreTask {

    private static final Logger logger = HTLog.get(WriteTask.class);

    public final SerializableModel toSave;
    public final CompletableFuture<Boolean> response;

    public WriteTask(String repoName, SerializableModel toSave, CompletableFuture<Boolean> response) {
        super(repoName);
        this.toSave = toSave;
        this.response = response;
    }

    @Override
    public void run() {
        response.complete(save(repoId, toSave));
    }

    private boolean save(String repoId, SerializableModel model) {
        String output = new Gson().toJson(model);
        boolean corruptedJson = RepoStore.write(repoId, output, model.issues.size());
        logger.info(HTLog.format(repoId, "Written to JSON store"));
        return corruptedJson;
    }
}

