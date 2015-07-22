package tests;

import backend.resource.serialization.SerializableModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;
import util.Utility;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoadAndPrintTest {

    @Test
    public void loadAndPrintTest() {
        // Load the error json from the project root, not from store folder
        Path errorJson = Paths.get("HubTurbo-HubTurbo.json");
        if (Files.exists(errorJson)) {
            // Continue only if the error json exists
            Optional<String> fileText = Utility.readFile("HubTurbo-HubTurbo.json");
            if (fileText.isPresent()) {
                SerializableModel sModel = new Gson().fromJson(fileText.get(),
                        new TypeToken<SerializableModel>(){}.getType());
//                System.out.println(sModel.toString());
            }
        }
    }

}
