package util;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * A helper that manages conversion from objects to JSON Strings, and vice versa.
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
     * Configures the gson object that will serialise and deserialise JSON data
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
     * Creates an instance of T from a JSON-formatted String
     * @param json The JSON-formatted string representation of an instance of T
     * @param instanceClass The class of the object to create from the JSON string
     * @param <T> The generic type to create an instance of
     * @return The instance of T with the specified values in the JSON string
     */
    public <T> T fromJsonString(String json, Class<T> instanceClass) {
        T createdInstance = gson.fromJson(json, instanceClass);
        logger.info("Instance successfully created: " + instanceClass.toString());
        return createdInstance;
    }

    /**
     * Creates a JSON-formatted string from the values in a T instance
     * @param instance The T object to be converted into the JSON string
     * @param instanceClass The class of the object
     * @param <T> The generic type to create an instance of
     * @return The JSON-formatted string that contains the values of the given T instance
     */
    public <T> String toJsonString(T instance, Class<T> instanceClass) {
        String json = gson.toJson(instance, instanceClass);
        logger.info("Successfully created json of: " + instanceClass.toString());
        return json;
    }
}
