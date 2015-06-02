package prefs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.IRepositoryIdProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstractions for the contents of the session file.
 */
@SuppressWarnings("unused")
public class SessionConfiguration {

	private static final Logger logger = LogManager.getLogger(SessionConfiguration.class.getName());

	private HashMap<String, List<String>> projectFilters = new HashMap<>();
	private List<RepoViewRecord> lastViewedRepositories = new ArrayList<>();
	private String lastLoginUsername = "";
	private byte[] lastLoginEncrypted = new byte[0];
	private String lastLoginPassword = "";
	
	public SessionConfiguration() {
	}
	
	public void setFiltersForNextSession(IRepositoryIdProvider project, List<String> filter) {
		if (project != null) {
			projectFilters.put(project.generateId().toLowerCase(), filter);
		}
	}
	
	public List<String> getFiltersFromPreviousSession(IRepositoryIdProvider project) {
		if (project == null) {
			return new ArrayList<>();
		}
		return projectFilters.get(project.generateId().toLowerCase());
	}
	
	/**
	 * Adds a repository to the list of last-viewed repositories.
	 * The list will always have 10 or fewer items.
	 */
	public void addToLastViewedRepositories(String repository) {
		repository = repository.toLowerCase();
		
		// Create record for this repository
		RepoViewRecord latestRepoView = new RepoViewRecord(repository);
		int index = lastViewedRepositories.indexOf(latestRepoView);
		if (index < 0) {
			lastViewedRepositories.add(latestRepoView);
		} else {
			lastViewedRepositories.get(index).setTimestamp(latestRepoView.getTimestamp());
		}
		
		// Keep only the 10 latest records
		Collections.sort(lastViewedRepositories);
		while (lastViewedRepositories.size() > 10) {
			lastViewedRepositories.remove(lastViewedRepositories.size() - 1);
		}
		assert lastViewedRepositories.size() <= 10;
	}
	
	/**
	 * Returns last-viewed repositories in owner/name format.
	 * They are sorted by access date, latest first.
	 */
	public List<String> getLastViewedRepositories() {
		return lastViewedRepositories.stream()
				.map(RepoViewRecord::getRepository)
				.collect(Collectors.toList());
	}

	public String getLastLoginUsername() {
		return lastLoginUsername;
	}

	public void setLastLoginUsername(String lastLoginUsername) {
		this.lastLoginUsername = lastLoginUsername;
	}
	
	public void setLastLoginPassword(String lastPassword){
		this.lastLoginPassword = encryptData(lastPassword);
	}

	public String getLastLoginPassword() {
		return decryptData();
	}

	private String encryptData(String lastPassword) {
		String result = "";
		try {
			String key = "HubTurboHubTurbo";
		    Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, aesKey);
			lastLoginEncrypted = cipher.doFinal(lastPassword.getBytes());
			result = new String(lastLoginEncrypted);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			logger.error("Cannot encrypt data " + e.getMessage(), e);
		}
		return result;
	}

	private String decryptData() {
		String result = "";
		try {
			String key = "HubTurboHubTurbo";
		    Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, aesKey);
			result = new String(cipher.doFinal(lastLoginEncrypted));
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			logger.error("Cannot encrypt data " + e.getMessage(), e);
		}
		return result;
	}
}
