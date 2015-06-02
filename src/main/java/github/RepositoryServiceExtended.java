package github;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class RepositoryServiceExtended extends RepositoryService{
	private static final Logger logger = LogManager.getLogger(RepositoryServiceExtended.class.getName());

	private GitHubClientExtended ghClient;
	private OrganizationService orgService;
	
	public RepositoryServiceExtended(GitHubClientExtended ghClient){
		this.ghClient = ghClient;
		orgService = new OrganizationService(this.ghClient);
	}
	
	private List<User> getOrganisations() throws IOException{
		return orgService.getOrganizations();
	}
	
	private List<Repository> getOrganisationRepos(User org){
		try {
			return getOrgRepositories(org.getLogin());
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return new ArrayList<>();
	}
	
	public List<Repository> getOrganisationRepositories() throws IOException{
		List<User> orgs = getOrganisations();
		return orgs.stream()
			.map(this::getOrganisationRepos)
			.reduce(new ArrayList<>(),
							(l, item) -> {l.addAll(item); 
										  return l;});
	}
	
	/**
	 * Returns a list of all repositories the user owns/contributes 
	 * and repositories belonging to organisations the user is a member of
	 * */
	
	public List<Repository> getAllRepositories(String user) throws IOException{
		HashSet<Repository> result = new HashSet<>(getRepositories(user));
		result.addAll(getOrganisationRepositories());
		return new ArrayList<>(result);
	}
	
	public List<String> getAllRepositoriesNames(String user) throws IOException{
		return getAllRepositories(user).stream()
									   .map(Repository::getName)
									   .collect(Collectors.toList());
	}
	
	public List<String> getRepositoriesNames(String user) throws IOException{
		return getRepositories(user).stream()
								.map(Repository::getName)
								.collect(Collectors.toList());
	}
	
}
