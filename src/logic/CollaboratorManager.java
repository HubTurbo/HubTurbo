package logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;

public class CollaboratorManager {
	private CollaboratorService service;
	private GitHubClient client;
	
	CollaboratorManager(GitHubClient client) {
		this.client = client;
		this.service = new CollaboratorService(client);
	}
	
	List<TurboCollaborator> getAllCollaborators(IRepositoryIdProvider repository) {
		List<TurboCollaborator> turboCollaborators = new ArrayList<TurboCollaborator>();
		try {
			List<User> collaborators = service.getCollaborators(repository);
			for(User collaborator : collaborators) {
				turboCollaborators.add(new TurboCollaborator(collaborator));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return turboCollaborators;
	}
}
