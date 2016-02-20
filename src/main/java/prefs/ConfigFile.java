package prefs;

import com.google.gson.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.HTLog;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * A general class for HubTurbo config files.
 * Handles serialisation and deserialisation of config objects from and to files.
 *
 * To retrieve specific config data that is stored in a file:
 * // example 1 - GlobalConfig type
 * ConfigFile cfGlobal = new ConfigFile("config/dir", "globalconfigfile.name");
 * GlobalConfig gc = (GlobalConfig) cfGlobal.loadConfig(new GlobalConfig());
 *
 * // example 2 - UserConfig type
 * ConfigFile cfUser = new ConfigFile("config/dir", "userconfigfile.name");
 * UserConfig gc = (UserConfig) cfUser.getConfig(new UserConfig());
 *
 * To save a specified Config object to a file:
 * ConfigFile cfGlobal = new ConfigFile("config/dir", "globalconfigfile.name");
 * cfGlobal.saveConfig(globalConfig);
 */
public class ConfigFile {

    private static final Logger logger = LogManager.getLogger(ConfigFile.class.getName());

    private static final String CHARSET = "UTF-8";
    private final String configFileName;
    private final String configDirectory;

    private Gson gson;

    /**
     * Constructs a new Config File that represents a specific Config type.
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
    private File getConfigFile() {
        return new File(configDirectory, configFileName).getAbsoluteFile();
    }

    //--- Public methods

    /**
     * Saves the config object to a file.
     * @param config The config object to be saved.
     */
    public void saveConfig(Config config) {
        File configFile = getConfigFile();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), CHARSET)) {
            gson.toJson(config, config.getClass(), writer);
            writer.close();
        } catch (IOException e) {
            HTLog.error(logger, e);
        }
    }

    /**
     * Loads a config object from a config file.
     * @param config The config object of the type to load
     * @return A new config object that contains the data in the config file.
     */
    public Config loadConfig(Config config) {
        File configFile = getConfigFile();
        try {
            if (!configFile.exists()) {
                boolean fileCreated = configFile.createNewFile();
                if (!fileCreated) {
                    logger.warn("Failed to create file!" + configFile.toString());
                }
            }
            Reader reader = new InputStreamReader(new FileInputStream(configFile), CHARSET);
            return gson.fromJson(reader, config.getClass());

        } catch (IOException e) {
            HTLog.error(logger, e);
            // Return the original config object since we cannot do anything
            return config;
        }
    }
}
