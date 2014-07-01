package util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.service.IssueService;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_ISSUES;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

public class IssueServiceExtended extends IssueService{
	private GitHubClientExtended ghClient;
	
	public static final String BODY_TAG = "issue[body]";
	
	public IssueServiceExtended(GitHubClientExtended client){
		super(client);
		this.ghClient = client;
	}
	
	public boolean editIssueDescription(IRepositoryIdProvider repository, int issueId, String oldDesc, String newDesc){
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repository.generateId());
		uri.append(SEGMENT_ISSUES);
		uri.append('/').append(issueId);
		try {
			HttpURLConnection connection = ghClient.createPost(uri.toString());
			String hash = generateIssueBodyHash(oldDesc);
			
			if(hash != null){
				connection.setRequestProperty("X-Body-Version", hash);
			}
			
			ghClient.sendParams(connection, createIssueDescriptionMap(newDesc));
			
			int responseCode = connection.getResponseCode();

			return ghClient.isOk(responseCode);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	protected Map<Object, Object> createIssueDescriptionMap(String desc){
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(FIELD_BODY, desc);
		return params;
	}
	
	private String generateIssueBodyHash(String body){
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(body.getBytes());
			 
	        byte byteData[] = md.digest();
	 
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < byteData.length; i++) {
	        	sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	        }
	 
	        return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
	}
	
}
