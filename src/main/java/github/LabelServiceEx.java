package github;

import com.google.gson.reflect.TypeToken;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.LabelService;

import java.io.IOException;
import java.util.List;

import static org.eclipse.egit.github.core.client.IGitHubConstants.*;


public class LabelServiceEx extends LabelService {

    public LabelServiceEx() {
        super();
    }

    public LabelServiceEx(GitHubClient client) {
        super(client);
    }

    public void deleteLabelFromIssue(IRepositoryIdProvider repository, String issueId,
                                     Label label) throws IOException {

//        Github api format: DELETE /repos/:owner/:repo/issues/:number/labels/:name
        String repoId = getId(repository);
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(repoId)
                .append(SEGMENT_ISSUES)
                .append('/').append(issueId)
                .append('/').append(label.getName());
        client.delete(uri.toString());
    }

    public List<Label> addLabelsToIssue(IRepositoryIdProvider repository,
                                        String issueId, List<Label> labels) throws IOException {
        String repoId = getId(repository);
        return addLabelsToIssue(repoId, issueId, labels);
    }

    private List<Label> addLabelsToIssue(String id, String issueId, List<Label> labels)
            throws IOException {
        if (issueId == null) {
            throw new IllegalArgumentException("Issue id cannot be null");
        }
        if (issueId.length() == 0) {
            throw new IllegalArgumentException("Issue id cannot be empty");
        }

//        POST /repos/:owner/:repo/issues/:number/labels

        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id)
                .append(SEGMENT_ISSUES)
                .append('/').append(issueId)
                .append(SEGMENT_LABELS);

        return client.post(uri.toString(), labels, new TypeToken<List<Label>>() {
        }.getType());
    }


    /**
     * @param repository
     * @param label      label with edited fields
     * @param name       name of label to edit
     * @return
     * @throws IOException
     */
    public Label editLabel(IRepositoryIdProvider repository, Label label, String name)
            throws IOException {
        String repoId = getId(repository);
        if (label == null) {
            throw new IllegalArgumentException("Label cannot be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("Label name cannot be null"); //$NON-NLS-1$
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException("Label name cannot be empty"); //$NON-NLS-1$
        }

        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(repoId)
                .append(SEGMENT_LABELS)
                .append('/').append(name);

        return client.post(uri.toString(), label, Label.class);
    }

}
