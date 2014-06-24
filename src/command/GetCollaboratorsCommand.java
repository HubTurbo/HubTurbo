package command;

import java.io.IOException;
import java.util.List;

import model.TurboCollaborator;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;

public class GetCollaboratorsCommand implements Command {

	private CollaboratorService service;
	private IRepositoryIdProvider repoId;
	private List<TurboCollaborator> collaborators;
	
	public GetCollaboratorsCommand(GitHubClient ghClient, IRepositoryIdProvider repoId, List<TurboCollaborator> collaborators) {
		this.service = new CollaboratorService(ghClient);
		this.repoId = repoId;
		this.collaborators = collaborators;
	}

	@Override
	public void execute() {
		try {
			List<User> ghCollaborators = service.getCollaborators(repoId);
			for(User ghCollaborator : ghCollaborators) {
				collaborators.add(new TurboCollaborator(ghCollaborator));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
