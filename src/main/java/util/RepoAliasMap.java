package util;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

/**
 * This class acts like a bi-directional map that maps repo ids with their aliases, if the aliases exist.
 * The mapping is a bijection, i.e. a repo id can only have one alias, and an alias can only be mapped to one repo id.
 *
 * This is a singleton class. Use the getInstance method to retrieve the instance.
 *
 * The mapping of aliases are pulled from a mapping file in the settings directory.
 */
public class RepoAliasMap {

    // Original mapping:
    //   Key = repoID, Value = alias.
    private final HashBiMap<String, String> aliasMap;

    // Static attributes
    //------------------

    private static final Logger logger = LogManager.getLogger(RepoAliasMap.class.getName());

    // The sole instance of the map
    private static RepoAliasMap instance;

    // The sole test instance for the map
    private static RepoAliasMap testInstance;

    // the mapping array length must be 2 because it is a bijection. Example: ["key", "value"]
    private static final int MAPPING_ARRAY_LENGTH = 2;
    private static final int MAPPING_ARRAY_KEY_INDEX = 0;
    private static final int MAPPING_ARRAY_VALUE_INDEX = 1;

    // Static methods
    //---------------

    /**
     * Since there is only one instance of the RepoAliasMap, this method retrieves it.
     * The instance is instantiated if it has not been yet.
     * @return The sole instance of the RepoAliasMap
     */
    public static RepoAliasMap getInstance() {
        if (instance == null) {
            instance = new RepoAliasMap();
            instance.updateMappings(getMappingsArrayFromFile("settings", "repo_alias_mapping.json"));
        }
        return instance;
    }

    /**
     * Gets a test instance of the map. For testing purposes
     * @return the test version of the map
     */
    public static RepoAliasMap getTestInstance() {
        if (testInstance == null) {
            testInstance = new RepoAliasMap();
            testInstance.updateMappings(getMappingsArrayFromFile("settings", "test_repo_alias_mapping.json"));
        }
        return testInstance;
    }

    /**
     * Reads the mapping file and returns a 2d String array of the mapping
     * @return the 2d String array representation of the mappings
     */
    private static String[][] getMappingsArrayFromFile(String mappingFileDirectoryName, String mappingFileName) {
        Gson gson = new GsonBuilder().create();
        File mappingsFile = new File(mappingFileDirectoryName, mappingFileName);

        try (Reader reader = new InputStreamReader(new FileInputStream(mappingsFile), "UTF-8")) {
            String[][] array = gson.fromJson(reader, String[][].class);
            reader.close();
            return array;

        } catch (IOException e) {
            e.printStackTrace();
            HTLog.error(logger, e);
            return null;
        }
    }


    // Instance methods
    //-----------------

    /**
     * Private constructor due to singleton class structure
     */
    private RepoAliasMap() {
        aliasMap = HashBiMap.create();
    }

    /**
     * Replaces all current mappings with the new mappings in the mappings array.
     * @param allMappings The new mappings; the inner arrays must be of length 2
     */
    private void updateMappings(String[][] allMappings) {
        assert allMappings != null;
        for (int i = 0; i < allMappings.length; i++) {
            String[] mapping = allMappings[i];
            assert mapping != null;

            if (mapping.length != MAPPING_ARRAY_LENGTH) {
                logger.warn("Repo alias mapping array is not the correct length! " +
                        "Detected length: " + mapping.length + ". " +
                        "Repo aliases may be wrong or not assigned.");
            }
            if (mapping.length >= MAPPING_ARRAY_LENGTH) {
                aliasMap.put(mapping[MAPPING_ARRAY_KEY_INDEX], mapping[MAPPING_ARRAY_VALUE_INDEX]);
            }
        }
    }

    /**
     * Produces a 2d string array representation of the mappings.
     * @return 2d string representation of the mappings.
     */
    public String[][] toMappingsArray() {
        String[][] allMappings = new String[aliasMap.size()][MAPPING_ARRAY_LENGTH];
        int i = 0;
        for (String repoId : aliasMap.keySet()) {
            String[] mapping = new String[MAPPING_ARRAY_LENGTH];
            mapping[MAPPING_ARRAY_KEY_INDEX] = repoId;
            mapping[MAPPING_ARRAY_VALUE_INDEX] = aliasMap.get(repoId);
            allMappings[i] = mapping;
            i++;
        }
        return allMappings;
    }

    /**
     * Checks whether a string is an alias of a repo or not
     * @param potentialAlias the potential alias to check
     * @return true if the given string is an alias
     */
    public boolean isAlias(String potentialAlias) {
        return aliasMap.containsValue(potentialAlias);
    }

    /**
     * Checks whether a repo id has an alias or not
     * @param repoId the repo id to check
     * @return true if the repo id has an alias
     */
    public boolean hasAlias(String repoId) {
        return aliasMap.containsKey(repoId);
    }

    /**
     * Gets the alias of the given repo id
     * @param repoId the repo id
     * @return the alias, or null if the mapping doesn't exist
     */
    public String getAlias(String repoId) {
        return aliasMap.get(repoId);
    }

    /**
     * Gets the repo id that is mapped to the supplied alias
     * @param alias the alias of the repo id
     * @return the repo id, or null if the mapping doesn't exist
     */
    public String getRepoId(String alias) {
        // use the inverse mapping (alias -> repoId)
        return aliasMap.inverse().get(alias);
    }

    /**
     * Returns the number of repo id to alias mappings in the map.
     * If the map contains more than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
     * @return the number of repo id to alias mappings in the map
     */
    public int size() {
        return aliasMap.size();
    }
}
