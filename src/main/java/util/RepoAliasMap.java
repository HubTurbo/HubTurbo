package util;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.BiMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * This class acts like a bi-directional map that maps repo ids with their aliases, if the aliases exist.
 * The mapping is a bijection i.e. a repo id can only have one alias, and an alias can only be mapped to one repo id.
 */
public final class RepoAliasMap {

    private static final Logger logger = LogManager.getLogger(RepoAliasMap.class.getName());

    // Original mapping:
    //   Key = repoId, Value = alias.
    // Inverted mapping:
    //   Key = alias, Value = repoId.
    private final BiMap<String, String> aliasMap;

    public RepoAliasMap(Map<String, String> map) {
        aliasMap = HashBiMap.create(map);
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
        assert Utility.isWellFormedRepoId(repoId);
        return aliasMap.get(repoId);
    }

    /**
     * Gets the repo id that is mapped to the supplied alias
     * @param alias the alias of the repo id
     * @return the repo id, or null if the mapping doesn't exist
     */
    public String getRepoId(String alias) {
        assert Utility.isWellFormedRepoAlias(alias);
        // use the inverse mapping (alias -> repoId)
        return aliasMap.inverse().get(alias);
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
        if (isAlias(repoIdOrAlias)) {
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
    public void addMapping(String repoId, String repoAlias) {
        assert Utility.isWellFormedRepoId(repoId);
        assert Utility.isWellFormedRepoAlias(repoAlias);
        aliasMap.put(repoId, repoAlias);
    }

    /**
     * Removes a mapping of repo id to alias.
     * Both values should be well formed according to their respective requirements.
     * @param repoId A well-formed repoId
     * @param repoAlias A well-formed alphanumeric repoAlias
     */
    public void removeMapping(String repoId, String repoAlias) {
        assert Utility.isWellFormedRepoId(repoId);
        assert Utility.isWellFormedRepoAlias(repoAlias);
        aliasMap.remove(repoId, repoAlias);
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
