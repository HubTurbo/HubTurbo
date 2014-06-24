package command;

import java.io.IOException;
import java.util.List;

import model.TurboLabel;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.LabelService;

public class GetLabelsCommand implements Command {
	
	private LabelService labelService;
	private IRepositoryIdProvider repoId;
	private List<TurboLabel> labels;
	
	public GetLabelsCommand(GitHubClient ghClient, IRepositoryIdProvider repoId, List<TurboLabel> labels) {
		this.labelService = new LabelService(ghClient);
		this.repoId = repoId;
		this.labels = labels;
	}
	
	@Override
	public void execute() {
		try {
			List<Label> ghLabels = labelService.getLabels(repoId);
			for (Label ghLabel : ghLabels) {
				labels.add(new TurboLabel(ghLabel));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
