package tests;

import static org.junit.Assert.*;

import org.junit.Test;
import prefs.Config;
import prefs.ConfigFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigFileTest {

    private static final String ENCODING = "UTF-8";

    private static final String HUBTURBO_REPO_ID = "HubTurbo/HubTurbo";
    private static final String HUBTURBO_REPO_ALIAS = "ht";

    private static final String POWERPOINTLABS_REPO_ID = "PowerPointLabs/PowerPointLabs";
    private static final String POWERPOINTLABS_REPO_ALIAS = "ppt";

    private static final String DUMMY_REPO_ID = "Dummy/Dummy";
    private static final String DUMMY_REPO_ALIAS = "dy";

    private static final String DUMMY_CONFIG_DIRECTORY = "settings";
    private static final String DUMMY_CONFIG_FILENAME = "user.json";

    private static final String DUMMY_CONFIG_FILE_CONTENTS_NO_WHITESPACE =
            String.format("{\"repoAliases\":{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\"}}",
                    HUBTURBO_REPO_ID,
                    HUBTURBO_REPO_ALIAS,
                    POWERPOINTLABS_REPO_ID,
                    POWERPOINTLABS_REPO_ALIAS,
                    DUMMY_REPO_ID,
                    DUMMY_REPO_ALIAS);

    @Test
    public void dummyConfigImplementation_serialisation_ableToWriteFile() {
        // set up some repo alises and save the file
        DummyConfig dcSave = new DummyConfig();
        HashMap<String, String> aliases = new HashMap<>();
        aliases.put(HUBTURBO_REPO_ID, HUBTURBO_REPO_ALIAS);
        aliases.put(POWERPOINTLABS_REPO_ID, POWERPOINTLABS_REPO_ALIAS);
        aliases.put(DUMMY_REPO_ID, DUMMY_REPO_ALIAS);
        dcSave.setRepoAliases(aliases);
        ConfigFile cfhSave = new ConfigFile(DUMMY_CONFIG_DIRECTORY, DUMMY_CONFIG_FILENAME);
        cfhSave.saveConfig(dcSave);

        // retrieve the file contents with basic io
        try {
            Path dummyConfigFilePath = Paths.get(DUMMY_CONFIG_DIRECTORY, DUMMY_CONFIG_FILENAME);
            String contentNoWhitespace
                    = new String(Files.readAllBytes(dummyConfigFilePath), ENCODING)
                    .replaceAll("\\s", "");
            assertEquals(DUMMY_CONFIG_FILE_CONTENTS_NO_WHITESPACE, contentNoWhitespace);
        } catch (IOException e) {
            fail("File IO failed");
        }
    }

    /**
     * Tests that the dummy config class can be constructed from a file with the config information
     */
    @Test
    public void dummyConfigImplementation_deserialisation_ableToConstructConfig() {
        // write the file with basic io
        try {
            File dummyConfigFile = new File(DUMMY_CONFIG_DIRECTORY, DUMMY_CONFIG_FILENAME);
            Writer writer = new OutputStreamWriter(new FileOutputStream(dummyConfigFile), ENCODING);
            writer.write(DUMMY_CONFIG_FILE_CONTENTS_NO_WHITESPACE);
            writer.close();
        } catch (IOException e) {
            fail("File IO failed");
        }

        // read the file and retrieve the repo aliases
        ConfigFile cfhRead = new ConfigFile(DUMMY_CONFIG_DIRECTORY, DUMMY_CONFIG_FILENAME);
        DummyConfig dcRead = (DummyConfig) cfhRead.loadConfig(new DummyConfig());

        // check if the values are there
        assertTrue(dcRead.getRepoAliases().containsKey(HUBTURBO_REPO_ID));
        assertEquals(HUBTURBO_REPO_ALIAS, dcRead.getRepoAliases().get(HUBTURBO_REPO_ID));
        assertTrue(dcRead.getRepoAliases().containsKey(POWERPOINTLABS_REPO_ID));
        assertEquals(POWERPOINTLABS_REPO_ALIAS, dcRead.getRepoAliases().get(POWERPOINTLABS_REPO_ID));
        assertTrue(dcRead.getRepoAliases().containsKey(DUMMY_REPO_ID));
        assertEquals(DUMMY_REPO_ALIAS, dcRead.getRepoAliases().get(DUMMY_REPO_ID));
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
