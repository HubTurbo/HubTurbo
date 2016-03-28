package github;

import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.UserService;
import util.HTLog;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CollaboratorServiceEx extends CollaboratorService {

    private static final Logger logger = HTLog.get(PullRequestServiceEx.class);

    private final UserService userService = new UserService();

    public CollaboratorServiceEx(GitHubClient client) {
        super(client);
    }

    /**
     * Gets the list of collaborators using CollaboratorService
     * Then iterate through the list to get each collaborator using UserService
     * This is done because the GitHub response for GET /repos/:owner/:repo/collaborators
     * returns users with some of their attributes missing
     *
     * @param repository
     * @return list of collaborators
     * @throws IOException
     */
    @Override
    public List<User> getCollaborators(IRepositoryIdProvider repository) throws IOException {
        return super.getCollaborators(repository).stream()
                .map(user -> {
                    try {
                        return userService.getUser(user.getLogin());
                    } catch (IOException e) {
                        logger.warn("Unable to get full details for user " + user.getLogin());
                        return user;
                    }
                }).collect(Collectors.toList());
    }
}
