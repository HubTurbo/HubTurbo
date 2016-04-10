package github.update;


import com.google.gson.reflect.TypeToken;
import github.CollaboratorServiceEx;
import github.GitHubClientEx;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PagedRequest;
import util.HTLog;

import java.util.ArrayList;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COLLABORATORS;

public class UserUpdateService extends UpdateService<User> {

    private final Logger logger = HTLog.get(UserUpdateService.class);

    public UserUpdateService(GitHubClientEx client, String collabsETag) {
        super(client, SEGMENT_COLLABORATORS, collabsETag);
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
        logger.info("Requesting for " + repoId.generateId() + " collaborators' complete data");
        return new ArrayList<>(CollaboratorServiceEx.getCompleteUserData(super.getUpdatedItems(repoId)));
    }
}
