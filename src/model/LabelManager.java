package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.LabelService;

public class LabelManager {
	private LabelService service;
	private GitHubClient client;
	
	LabelManager(GitHubClient client) {
		this.client = client;
		service = new LabelService(client);
	}
	
	List<TurboLabel> getAllLabels(IRepositoryIdProvider repository) {
		List<TurboLabel> turboLabels = new ArrayList<TurboLabel>();
		try {
			List<Label> labels = service.getLabels(repository);
			for (Label label : labels) {
				turboLabels.add(new TurboLabel(label));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return turboLabels;
	}
}
