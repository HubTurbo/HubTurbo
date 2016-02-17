package util;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.BiMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * This class holds HubTurbo-specific configuration metadata for repositories.
 */
public final class RepoConfig {

    private static final Logger logger = LogManager.getLogger(RepoConfig.class.getName());

    // Original mapping:
    //   Key = repoId, Value = alias.
    // Inverted mapping:
    //   Key = alias, Value = repoId.
    // We'll need to cast to BiMap when we want to work with the inverse mapping
    // Problem is that Gson creates a linked tree map
    private final Map<String, String> repoIdToAliasMap = HashBiMap.create();

    /**
     * Checks whether a string is an alias of a repo or not
     * @param potentialAlias the potential alias to check
     * @return true if the given string is an alias
     */
    public boolean isRepoAlias(String potentialAlias) {
        return repoIdToAliasMap.containsValue(potentialAlias);
    }

    /**
     * Checks whether a repo id has an alias or not
     * @param repoId the repo id to check
     * @return true if the repo id has an alias
     */
    public boolean hasRepoAlias(String repoId) {
        return repoIdToAliasMap.containsKey(repoId);
    }

    /**
     * Gets the alias of the given repo id
     * @param repoId the repo id
     * @return the alias, or null if the mapping doesn't exist
     */
    public String getRepoAlias(String repoId) {
        assert Utility.isWellFormedRepoId(repoId);
        return repoIdToAliasMap.get(repoId);
    }

    /**
     * Gets the repo id that is mapped to the supplied alias
     * @param alias the alias of the repo id
     * @return the repo id, or null if the mapping doesn't exist
     */
    public String getRepoId(String alias) {
        assert Utility.isWellFormedRepoAlias(alias);
        // use the inverse mapping (alias -> repoId)
        BiMap<String, String> repoAliasBiMap = HashBiMap.create(repoIdToAliasMap);
        return repoAliasBiMap.inverse().get(alias);
    }

    /**
     * If an alias is supplied, this method resolves the alias against its repo id.
     * Otherwise it assumes that the given string is a repo id and simply returns it.
     * @param repoIdOrAlias
     * @return
     */
    public String resolveRepoId(String repoIdOrAlias) {
        String repoId;
        // initialise the repoId with the appropriate values
        if (isRepoAlias(repoIdOrAlias)) {
            String repoAlias = repoIdOrAlias;
            logger.info("Repo alias supplied: " + repoAlias);
            repoId = getRepoId(repoAlias);
            logger.info("Retrieved repo id: " + repoId);
        } else {
            repoId = repoIdOrAlias;
            logger.info("Not a repo alias: " + repoId + ", assume it to be repo id.");
        }
        return repoId;
    }

    /**
     * Add a mapping of repo id to alias.
     * Both values should be well formed according to their respective requirements.
     * @param repoId A well-formed repoId
     * @param repoAlias A well-formed alphanumeric repoAlias
     */
    public void addAliasMapping(String repoId, String repoAlias) {
        assert Utility.isWellFormedRepoId(repoId);
        assert Utility.isWellFormedRepoAlias(repoAlias);
        repoIdToAliasMap.put(repoId, repoAlias);
    }

    /**
     * Removes a mapping of repo id to alias.
     * Both values should be well formed according to their respective requirements.
     * @param repoId A well-formed repoId
     * @param repoAlias A well-formed alphanumeric repoAlias
     */
    public void removeAliasMapping(String repoId, String repoAlias) {
        assert Utility.isWellFormedRepoId(repoId);
        assert Utility.isWellFormedRepoAlias(repoAlias);
        repoIdToAliasMap.remove(repoId, repoAlias);
    }

    /**
     * Returns the number of repo id to alias mappings in the map.
     * If the map contains more than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
     * @return the number of repo id to alias mappings in the map
     */
    public int getAliasCount() {
        return repoIdToAliasMap.size();
    }
}
