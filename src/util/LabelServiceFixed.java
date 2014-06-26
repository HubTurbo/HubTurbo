package util;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_LABELS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import java.io.IOException;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.LabelService;

public class LabelServiceFixed extends LabelService {
	
	public LabelServiceFixed() {
		super();
	}

	public LabelServiceFixed(GitHubClient client) {
		super(client);
	}

	/**
	 * 
	 * @param repository
	 * @param label: label with edited fields
	 * @param name: name of label to edit
	 * @return
	 * @throws IOException
	 */
	public Label editLabel(IRepositoryIdProvider repository, Label label , String name)
			throws IOException {
		String repoId = getId(repository);
		if (label == null)
			throw new IllegalArgumentException("Label cannot be null"); //$NON-NLS-1$
		if (name == null)
			throw new IllegalArgumentException("Label name cannot be null"); //$NON-NLS-1$
		if (name.length() == 0)
			throw new IllegalArgumentException("Label name cannot be empty"); //$NON-NLS-1$

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repoId);
		uri.append(SEGMENT_LABELS);
		uri.append('/').append(name);

		return client.post(uri.toString(), label, Label.class);
	}
	
}
