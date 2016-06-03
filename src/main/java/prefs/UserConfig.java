package prefs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
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

    public void addRepo(RepoInfo repo) {
        if (repos.stream().noneMatch(e -> e.equals(repo))) {
            repos.add(repo);
        }
    }

    public void removeRepo(RepoInfo repo) {
        repos.removeIf((e) -> e.equals(repo));
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
