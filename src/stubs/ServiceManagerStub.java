package stubs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.Model;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;

import service.ServiceManager;
import service.updateservice.CommentUpdateService;

public class ServiceManagerStub extends ServiceManager{
	public static String TEST_GH_USERNAME = "testapi";
	public static String TEST_REPO_NAME = "issuetest";
	
	private static Model model = new ModelStub();;
	
	public ServiceManagerStub(){
	}
	
	public boolean login(String userId, String password){
		try {
			model.loadComponents(getRepoId());
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public boolean setupRepository(String owner, String name){
		try {
			return model.loadComponents(RepositoryId.create(owner, name));
		} catch (IOException e) {
			return false;
		}
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
	
	public RepositoryId getRepoId(){
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
	
	public List<Comment> getComments(int issueId) throws IOException{
		ArrayList<Comment> comments = new ArrayList<Comment>();
		User testUser = new User();
		testUser.setLogin("random guy");
		User anotherUser = new User();
		anotherUser.setLogin("another guy");
		
		
		Comment comment1 = new Comment();
		comment1.setBody("test comment");
		comment1.setId(1);
		comment1.setUser(testUser);
		comment1.setCreatedAt(new Date());
		
		Comment comment2 = new Comment();
		comment2.setBody("test comment 2");
		comment2.setId(3);
		comment2.setUser(anotherUser);
		comment2.setCreatedAt(new Date());
		
		comments.add(comment1);
		comments.add(comment2);
		
		return comments;
	}
}
