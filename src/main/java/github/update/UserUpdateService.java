package github.update;


import com.google.gson.reflect.TypeToken;
import github.CollaboratorServiceEx;
import github.GitHubClientEx;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.eclipse.egit.github.core.service.UserService;
import util.HTLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COLLABORATORS;

public class UserUpdateService extends UpdateService<User> {

    private final Logger logger = HTLog.get(UserUpdateService.class);
    private final CollaboratorServiceEx collaboratorService;

    public UserUpdateService(GitHubClientEx client, String collabsETag) {
        super(client, SEGMENT_COLLABORATORS, collabsETag);
        collaboratorService = new CollaboratorServiceEx(client);
    }

    @Override
    protected PagedRequest<User> createUpdatedRequest(IRepositoryIdProvider repoId) {
        PagedRequest<User> request = super.createUpdatedRequest(repoId);
        request.setType(new TypeToken<User>() {
        }.getType());
        request.setArrayType(new TypeToken<ArrayList<User>>() {
        }.getType());
        return request;
    }

    @Override
    public ArrayList<User> getUpdatedItems(IRepositoryIdProvider repoId) {
        logger.info("Requesting for collaborators' complete data");
        return new ArrayList<>(collaboratorService.getUsersCompleteData(super.getUpdatedItems(repoId)));
    }
}
