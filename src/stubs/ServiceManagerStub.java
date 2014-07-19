package stubs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubRequest;

import model.Model;
import service.ServiceManager;
import service.updateservice.CommentUpdateService;

public class ServiceManagerStub extends ServiceManager{
	public static String TEST_GH_USERNAME = "testapi";
	public static String TEST_REPO_NAME = "issuetest";
	
	private static Model model = new ModelStub();;
	
	public ServiceManagerStub(){
	}
	
	public boolean login(String userId, String password){
		model.loadComponents(getRepoId());
		return true;
	}
	
	public void setupRepository(String owner, String name){
		model.loadComponents(RepositoryId.create(owner, name));
	}
	
	public Model getModel(){
		return model;
	}
	
	public void setupAndStartModelUpdate() {
		
	}
	
	public void restartModelUpdate(){
		
	}
	
	public void stopModelUpdate(){
	
	}
	
	public IRepositoryIdProvider getRepoId(){
		return RepositoryId.create(TEST_GH_USERNAME, TEST_REPO_NAME);
	}
	
	public int getRemainingRequests(){
		return 100;
	}
	
	public int getRequestLimit(){
		return 100;
	}
	
	public CommentUpdateService getCommentUpdateService(int id, List<Comment> list){
		return null;
	}
	
	public List<Repository> getRepositories() throws IOException{
		return new ArrayList<Repository>();
	}
	
	public List<String> getRepositoriesNames() throws IOException{
		return new ArrayList<String>();
	}
	
	public List<Repository> getAllRepositories() throws IOException{
		return new ArrayList<Repository>();
	}
	
	public List<String> getAllRepositoryNames() throws IOException{
		return new ArrayList<String>();
	}
}
