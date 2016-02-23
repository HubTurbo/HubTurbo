package tests;

import static org.junit.Assert.*;

import org.junit.Test;
import prefs.Config;
import prefs.ConfigFile;
import prefs.GlobalConfig;
import prefs.Preferences;


import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigFileTest {

    private static final String CHARSET = "UTF-8";

    private static final String HUBTURBO_REPO_ID = "HubTurbo/HubTurbo";
    private static final String HUBTURBO_REPO_ALIAS = "ht";

    private static final String POWERPOINTLABS_REPO_ID = "PowerPointLabs/PowerPointLabs";
    private static final String POWERPOINTLABS_REPO_ALIAS = "ppt";

    private static final String DUMMY_REPO_ID = "Dummy/Dummy";
    private static final String DUMMY_REPO_ALIAS = "dy";

    private static final String DUMMY_CONFIG_DIRECTORY = Preferences.DIRECTORY;
    private static final String DUMMY_CONFIG_FILENAME = "dummyConfig.json";
    private static final String SESSION_CONFIG_DIRECTORY = Preferences.DIRECTORY;
    private static final String TEST_SESSION_CONFIG_FILENAME = Preferences.TEST_SESSION_CONFIG_FILENAME;

    private static final String DUMMY_CONFIG_FILE_CONTENTS_NO_WHITESPACE =
            String.format("{\"repoAliases\":{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\"}}",
                    HUBTURBO_REPO_ID,
                    HUBTURBO_REPO_ALIAS,
                    POWERPOINTLABS_REPO_ID,
                    POWERPOINTLABS_REPO_ALIAS,
                    DUMMY_REPO_ID,
                    DUMMY_REPO_ALIAS);

    private static final String DUMMY_BOARD_NAME = "DummyBoardName";

    /**
     * Test if the dummy config class can write its config information to a file
     */
    @Test
    public void dummyConfigImplementation_serialisation_ableToWriteFile() {
        // set up some repo alises and save the file
        HashMap<String, String> aliases = new HashMap<>();
        aliases.put(HUBTURBO_REPO_ID, HUBTURBO_REPO_ALIAS);
        aliases.put(POWERPOINTLABS_REPO_ID, POWERPOINTLABS_REPO_ALIAS);
        aliases.put(DUMMY_REPO_ID, DUMMY_REPO_ALIAS);

        DummyConfig dummyConfig = new DummyConfig();
        dummyConfig.setRepoAliases(aliases);
        ConfigFile dummyConfigFile = new ConfigFile(DUMMY_CONFIG_DIRECTORY, DUMMY_CONFIG_FILENAME);
        dummyConfigFile.saveConfig(dummyConfig);

        // retrieve the file contents with basic io
        try {
            Path dummyConfigFilePath = Paths.get(DUMMY_CONFIG_DIRECTORY, DUMMY_CONFIG_FILENAME);
            String contentNoWhitespace =
                    new String(Files.readAllBytes(dummyConfigFilePath), CHARSET).replaceAll("\\s", "");
            assertEquals(DUMMY_CONFIG_FILE_CONTENTS_NO_WHITESPACE, contentNoWhitespace);
        } catch (IOException e) {
            fail("File IO failed");
        }

        // clean up
        assertTrue(deleteFile(dummyConfigFile));
    }

    /**
     * Tests that the dummy config class can be constructed from a file with the config information
     */
    @Test
    public void dummyConfigImplementation_deserialisation_ableToConstructConfig() {
        // write the file with basic io
        try {
            Writer writer = new OutputStreamWriter(
                            new FileOutputStream(
                            new File(DUMMY_CONFIG_DIRECTORY, DUMMY_CONFIG_FILENAME)), CHARSET);
            writer.write(DUMMY_CONFIG_FILE_CONTENTS_NO_WHITESPACE);
            writer.close();
        } catch (IOException e) {
            fail("File IO failed");
        }

        // read the file and retrieve the repo aliases
        ConfigFile dummyConfigFile = new ConfigFile(DUMMY_CONFIG_DIRECTORY, DUMMY_CONFIG_FILENAME);
        DummyConfig dummyConfig =
                (DummyConfig) dummyConfigFile.loadConfig(DummyConfig.class).orElse(new DummyConfig());

        // check if the values are there
        assertTrue(dummyConfig.getRepoAliases().containsKey(HUBTURBO_REPO_ID));
        assertEquals(HUBTURBO_REPO_ALIAS, dummyConfig.getRepoAliases().get(HUBTURBO_REPO_ID));

        assertTrue(dummyConfig.getRepoAliases().containsKey(POWERPOINTLABS_REPO_ID));
        assertEquals(POWERPOINTLABS_REPO_ALIAS, dummyConfig.getRepoAliases().get(POWERPOINTLABS_REPO_ID));

        assertTrue(dummyConfig.getRepoAliases().containsKey(DUMMY_REPO_ID));
        assertEquals(DUMMY_REPO_ALIAS, dummyConfig.getRepoAliases().get(DUMMY_REPO_ID));

        // clean up
        assertTrue(deleteFile(dummyConfigFile));
    }

    /**
     * Tests that a blank file produces the value in the orElse method argument
     */
    @Test
    public void dummyConfigImplementation_deserialisationFromBlankFile_ableToConstructConfig() {
        try {
            Writer writer = new OutputStreamWriter(
                            new FileOutputStream(
                            new File(DUMMY_CONFIG_DIRECTORY, DUMMY_CONFIG_FILENAME)), CHARSET);
            writer.write("");
            writer.close();

            // create a object to return in the orElse method.
            DummyConfig elseDummyConfig = new DummyConfig();

            // read the blank file
            ConfigFile dummyConfigFile = new ConfigFile(DUMMY_CONFIG_DIRECTORY, DUMMY_CONFIG_FILENAME);
            DummyConfig dummyConfig =
                    (DummyConfig) dummyConfigFile.loadConfig(DummyConfig.class).orElse(elseDummyConfig);

            // ensure the config returned matches
            assertEquals(elseDummyConfig, dummyConfig);

            // clean up
            assertTrue(deleteFile(dummyConfigFile));

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Tests that the actual session config can be saved and read from the disk file
     */
    @Test
    public void sessionConfig_serialisationAndDeserialisation_ableToSaveAndConstructConfig() {
        // set up the session config and write to file
        ConfigFile sessionConfigFile = new ConfigFile(SESSION_CONFIG_DIRECTORY, TEST_SESSION_CONFIG_FILENAME);
        GlobalConfig sessionConfigToSave = new GlobalConfig();
        sessionConfigToSave.setLastOpenBoard(DUMMY_BOARD_NAME);
        sessionConfigToSave.setLastViewedRepository(DUMMY_REPO_ID);
        sessionConfigFile.saveConfig(sessionConfigToSave);

        // read from file and check existing values
        GlobalConfig sessionConfig;
        sessionConfig = (GlobalConfig) sessionConfigFile.loadConfig(GlobalConfig.class).orElse(new GlobalConfig());
        assertEquals(DUMMY_BOARD_NAME, sessionConfig.getLastOpenBoard().get());
        assertEquals(DUMMY_REPO_ID, sessionConfig.getLastViewedRepository());

        // clean up
        assertTrue(deleteFile(sessionConfigFile));
    }

    /**
     * Attempts to delete the config file
     * @param configFile The ConfigFile to be deleted
     * @return True if and only if the file is deleted; False otherwise.
     */
    private boolean deleteFile(ConfigFile configFile) {
        try {
            Method methodGetConfigFile = ConfigFile.class.getDeclaredMethod("getConfigFileInDisk");
            methodGetConfigFile.setAccessible(true);
            File configFileInDisk = (File) methodGetConfigFile.invoke(configFile);
            return configFileInDisk.delete();
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            fail("Cannot delete file");
            return false;
        }
    }






    /**
     * Dummy config class for testing purposes, ensures that classes that implement config can be
     * properly handled by the ConfigFile class
     */
    private static class DummyConfig implements Config {
        private Map<String, String> repoAliases = new HashMap<>();

        public Map<String, String> getRepoAliases() {
            return new HashMap<>(repoAliases);
        }

        public void setRepoAliases(Map<String, String> aliases) {
            repoAliases = new HashMap<>(aliases);
        }
    }
}
