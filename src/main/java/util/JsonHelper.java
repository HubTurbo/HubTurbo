package util;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import prefs.Config;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * A helper that manages conversion from Config files to JSON Strings, and vice versa.
 */
public class JsonHelper {
    private static final Logger logger = LogManager.getLogger(JsonHelper.class.getName());

    private Gson gson;

    /**
     * Sets up the JsonHelper by initialising the Gson component
     */
    public JsonHelper() {
        setupGson();
    }

    /**
     * Configures the gson object that will serialise and deserialise json data
     */
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
                        })
                .create();
    }

    /**
     * Creates a specific Config subclass instance from a JSON-formatted String
     * @param jsonString The String in JSON format that contains the Config values
     * @param configClass The class of Config to create from the JSON
     * @return The Config instance of the specified class
     */
    public <T extends Config> T createConfigFromJson(String jsonString, Class<T> configClass)
            throws IllegalAccessException, InstantiationException {

        if (jsonString.isEmpty()) {
            logger.warn("Empty json string supplied, creating new Config instance");
            return configClass.newInstance();
        } else {
            T createdConfig = gson.fromJson(jsonString, configClass);
            logger.info("Config successfully created");
            return createdConfig;
        }
    }

    /**
     * Creates a JSON-formatted string from the values in a Config instance
     * @param config The Config object to be converted into the JSON string
     * @return The JSON-formatted string that contains the values of the given Config
     */
    public String createJsonFromConfig(Config config) {
        String json = gson.toJson(config, config.getClass());
        logger.info("Successfully created json");
        return json;
    }
}
