package prefs;

import com.google.gson.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.HTLog;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * A general class for HubTurbo config files.
 * Handles serialisation and deserialisation of config objects from and to files.
 */
public class ConfigFile {

    private static final Logger logger = LogManager.getLogger(ConfigFile.class.getName());

    private static final String CHARSET = "UTF-8";
    private final String configFileName;
    private final String configDirectory;

    private Gson gson;

    /**
     * Constructs a new Config File located at the specified directory and filename.
     * @param configDirectory The directory that the config file is held in
     * @param configFileName The file name of the config file
     */
    public ConfigFile(String configDirectory, String configFileName) {
        this.configFileName = configFileName;
        this.configDirectory = configDirectory;

        setupGson();
        createDirectoryIfNotExist();
    }

    /**
     * configure the gson object that will serialise and deserialise json data
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
                        }
                ).create();
    }

    /**
     * Attempts to create the config directory if it does not exist.
     */
    private void createDirectoryIfNotExist() {
        File directory = new File(configDirectory);
        boolean directoryExists = directory.exists() && directory.isDirectory();
        if (!directoryExists) {
            if (directory.mkdirs()) {
                logger.info("Config directory created: " + directory.toString());
            } else {
                logger.warn("Could not create config file directory");
            }
        }
    }

    /**
     * Returns the File representation at the config file path
     */
    private File getConfigFileInDisk() {
        return new File(configDirectory, configFileName).getAbsoluteFile();
    }

    //--- Public methods

    /**
     * Saves the config object to a file in disk
     * @param config The config object to be saved.
     */
    public void saveConfig(Config config) {
        File configFileInDisk = getConfigFileInDisk();
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(configFileInDisk), CHARSET);
            gson.toJson(config, config.getClass(), writer);
            writer.close();
            logger.info("Save successful: " + configFileInDisk.toString());

        } catch (IOException e) {
            HTLog.error(logger, e);
            logger.warn("Save unsuccessful: " + configFileInDisk.toString());
        }
    }

    /**
     * Loads a config object from a config file in disk. The returned config object is wrapped in an Optional and may be
     * null. A null Config is returned when either the file does not exist, or exists but deserialisation results
     * in null.
     * @param configType The Type of Config to load
     * @return An Optional of nullable Config.
     */
    public Optional<Config> loadConfig(Class<? extends Config> configType) {
        File configFileInDisk = getConfigFileInDisk();
        if (!configFileInDisk.exists()) {
            logger.warn("Config file does not exist! " + configFileInDisk.toString());
            return Optional.empty();
        }
        try {
            Reader reader = new InputStreamReader(new FileInputStream(configFileInDisk), CHARSET);
            Optional<Config> loadedConfig = Optional.ofNullable(gson.fromJson(reader, configType));
            reader.close();
            logger.info("Load successful: " + configFileInDisk.toString());
            return loadedConfig;

        } catch (IOException e) {
            HTLog.error(logger, e);
            logger.warn("Config file could not be loaded! " + configFileInDisk.toString());
            return Optional.empty();
        }
    }
}
