package prefs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents user-defined settings.
 */
public class UserConfig {

    private static final Logger logger = LogManager.getLogger(UserConfig.class.getName());

    private List<RepoInfo> repos = new ArrayList<>();

    public List<RepoInfo> getRepos() {
        return repos;
    }

    /**
     * Adds a repo to the repo list only if the repo does not already exist in the list.
     *
     * Existence in the list is defined as having the same repo ID or the same alias as
     * another repo already in the list. This check makes the addition run in O(n).
     *
     * The final result should still be that the repo to be added will be in the repo
     * list anyway.
     * @param repo The repo object to be added to the list.
     */
    public void addRepo(RepoInfo repo) {
        logger.info("Attempting to add repo: " + repo.toString());
        if (repos.stream().noneMatch(e -> (e.hasSameRepoId(repo) || e.hasSameAlias(repo)))) {
            repos.add(repo);
            logger.info("Added repo: " + repo.toString());
        } else {
            logger.info("Repo already exists: " + repo.toString());
        }
    }

    public void removeRepo(RepoInfo repo) {
        logger.info("Current list: " + repos.toString());
        logger.info("Attempting to remove repo: " + repo.toString());
        repos.removeIf(e -> (e.hasSameRepoId(repo) || e.hasSameAlias(repo)));
        logger.info("Current list: " + repos.toString());
    }

    public RepoInfo getRepoById(String repoId) {
        return repos.stream()
                    .filter(e -> e.getId() == repoId)
                    .collect(Collectors.toList())
                    .get(0);
    }

    public RepoInfo getRepoByAlias(String repoAlias) {
        return repos.stream()
                    .filter(e -> e.getAlias() == repoAlias)
                    .collect(Collectors.toList())
                    .get(0);
    }

}
