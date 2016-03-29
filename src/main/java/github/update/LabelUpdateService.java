package github.update;

import com.google.gson.reflect.TypeToken;
import github.GitHubClientEx;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.PagedRequest;

import java.util.ArrayList;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_LABELS;

public class LabelUpdateService extends UpdateService<Label> {
    public LabelUpdateService(GitHubClientEx client, String labelsETag) {
        super(client, SEGMENT_LABELS, labelsETag);
    }

    @Override
    protected PagedRequest<Label> createUpdatedRequest(IRepositoryIdProvider repoId) {
        PagedRequest<Label> request = super.createUpdatedRequest(repoId);
        request.setType(new TypeToken<Label>() {
        }.getType());
        request.setArrayType(new TypeToken<ArrayList<Label>>() {
        }.getType());
        return request;
    }
}
