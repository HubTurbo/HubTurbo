package util;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Converts JSON file from and to object
 */
public class JsonFileConverter {
    private static final Logger logger = LogManager.getLogger(JsonFileConverter.class.getName());
    private static final String CHARSET = "UTF-8";

    private Gson gson;
    private final File jsonFile;

    public JsonFileConverter(File jsonFile) {
        this.jsonFile = jsonFile;
        setupGson();
    }

    /**
     * Save a given object into JSON file
     * @param object object to be saved to JSON file
     */
    public void saveToFile(Object object) throws IOException {
        createDirectories();
        Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile) , CHARSET);
        gson.toJson(object, object.getClass(), writer);
        writer.close();
    }

    /**
     * Loads an object from a JSON file
     * @param objectClass class of the object to be loaded; used to parse the JSON
     * @return The object from JSON file, or Optional.empty() if the file can't be parsed
     */
    public <T> Optional<T> loadFromFile(Class<T> objectClass) {
        if (!jsonFile.exists()) {
            logger.info("JSON file %1$s does not exist", jsonFile.getName());
            return Optional.empty();
        }

        try (Reader reader =
                     new InputStreamReader(new FileInputStream(jsonFile), CHARSET)) {
            return Optional.ofNullable(gson.fromJson(reader, objectClass));
        } catch (IOException e) {
            logger.error(" Failed to load object from JSON file", e);
            return Optional.empty();
        }
    }

    private void setupGson() {
        gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> {
                    Instant instant = src.atZone(ZoneId.systemDefault()).toInstant();
                    long epochMilli = instant.toEpochMilli();
                    return new JsonPrimitive(epochMilli);
                })
            .registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
                    Instant instant = Instant.ofEpochMilli(json.getAsJsonPrimitive().getAsLong());
                    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                }).create();
    }

    private void createDirectories() throws IOException {
        if (jsonFile.getParentFile() != null) {
            File directory = new File(jsonFile.getParent());

            if ((!directory.exists() || !directory.isDirectory()) && !directory.mkdirs()) {
                throw new IOException("Could not create config file directory");
            }
        }
    }
}
