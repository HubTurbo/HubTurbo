package tests.stubs;

import model.Model;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import service.ServiceManager;
import tests.ServiceManagerTests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServiceManagerStub extends ServiceManager {

	public ServiceManagerStub() {
		model = new Model();
	}

	@Override
	public boolean isRepositoryValid(IRepositoryIdProvider repo) throws IOException {
		return true;
	}

	@Override
	public void stopModelUpdate() {

	}

	@Override
	public RepositoryId getRepoId() {
		return RepositoryId.create(ServiceManagerTests.TEST_GITHUB_USERNAME, ServiceManagerTests.TEST_REPO_NAME);
	}

	@Override
	public List<Repository> getRepositories() throws IOException {
		return new ArrayList<>();
	}

	@Override
	public List<String> getRepositoriesNames() throws IOException {
		return new ArrayList<>();
	}

	@Override
	public List<Repository> getAllRepositories() throws IOException {
		return new ArrayList<>();
	}

	@Override
	public List<String> getAllRepositoryNames() throws IOException {
		return new ArrayList<>();
	}
}
