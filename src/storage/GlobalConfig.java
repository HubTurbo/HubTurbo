package storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * Abstractions for the contents of the global config file.
 */
@SuppressWarnings("unused")
public class GlobalConfig {

	private static final Logger logger = LogManager.getLogger(GlobalConfig.class.getName());

//	private HashMap<String, List<String>> projectFilters = new HashMap<>();
//	private List<RepoViewRecord> lastViewedRepositories = new ArrayList<>();
	private String lastLoginUsername = "";
	private byte[] lastLoginPassword = new byte[0];

	public GlobalConfig() {
	}
	
//	public void setFiltersForNextSession(IRepositoryIdProvider project, List<String> filter) {
//		if (project != null) {
//			projectFilters.put(project.generateId().toLowerCase(), filter);
//		}
//	}
//
//	public List<String> getFiltersFromPreviousSession(IRepositoryIdProvider project) {
//		if (project == null) {
//			return new ArrayList<>();
//		}
//		return projectFilters.get(project.generateId().toLowerCase());
//	}
//
//	/**
//	 * Adds a repository to the list of last-viewed repositories.
//	 * The list will always have 10 or fewer items.
//	 */
//	public void addToLastViewedRepositories(String repository) {
//		repository = repository.toLowerCase();
//
//		// Create record for this repository
//		RepoViewRecord latestRepoView = new RepoViewRecord(repository);
//		int index = lastViewedRepositories.indexOf(latestRepoView);
//		if (index < 0) {
//			lastViewedRepositories.add(latestRepoView);
//		} else {
//			lastViewedRepositories.get(index).setTimestamp(latestRepoView.getTimestamp());
//		}
//
//		// Keep only the 10 latest records
//		Collections.sort(lastViewedRepositories);
//		while (lastViewedRepositories.size() > 10) {
//			lastViewedRepositories.remove(lastViewedRepositories.size() - 1);
//		}
//		assert lastViewedRepositories.size() <= 10;
//	}
//
//	/**
//	 * Returns last-viewed repositories in owner/name format.
//	 * They are sorted by access date, latest first.
//	 */
//	public List<String> getLastViewedRepositories() {
//		return lastViewedRepositories.stream()
//				.map(repoViewRecord -> repoViewRecord.getRepository())
//				.collect(Collectors.toList());
//	}
//
//	public String getLastLoginUsername() {
//		return lastLoginUsername;
//	}
//
//	public void setLastLoginUsername(String lastLoginUsername) {
//		this.lastLoginUsername = lastLoginUsername;
//	}
//
//	public void setLastLoginPassword(String lastPassword){
//		this.lastLoginPassword = encryptData(lastPassword);
//	}
//
	public String getLastLoginUsername() {
		return lastLoginUsername;
	}

	public String getLastLoginPassword() {
		return decrypt(lastLoginPassword);
	}

	public void setLastLoginCredentials(String username, String password) {
		this.lastLoginUsername = username;
		this.lastLoginPassword = encrypt(password);
	}

	private static byte[] encrypt(String lastPassword) {
		byte[] result = new byte[0];
		try {
			String key = "HubTurboHubTurbo";
		    Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, aesKey);
			result = cipher.doFinal(lastPassword.getBytes());
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			logger.error("Cannot encrypt data " + e.getMessage(), e);
		}
		return result;
	}

	private static String decrypt(byte[] lastLoginEncrypted) {
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
