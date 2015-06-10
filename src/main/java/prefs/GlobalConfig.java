package prefs;

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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstractions for the contents of the global config file.
 */
@SuppressWarnings("unused")
public class GlobalConfig {

	private static final Logger logger = LogManager.getLogger(GlobalConfig.class.getName());

	private List<String> lastOpenFilters = new ArrayList<>();
	private List<RepoViewRecord> lastViewedRepositories = new ArrayList<>();
	private String lastLoginUsername = "";
	private byte[] lastLoginPassword = new byte[0];
	private Map<String, List<String>> boards = new HashMap<>();
	private Map<String, Map<Integer, LocalDateTime>> markedReadTimes = new HashMap<>();

	public GlobalConfig() {
	}

	public void setMarkedReadAt(String repoId, int issue, LocalDateTime time) {
		if (!markedReadTimes.containsKey(repoId)) {
			markedReadTimes.put(repoId, new HashMap<>());
		}
		markedReadTimes.get(repoId).put(issue, time);
	}

	public Optional<LocalDateTime> getMarkedReadAt(String repoId, int issue) {
		if (!markedReadTimes.containsKey(repoId)) {
			return Optional.empty();
		}
		return Optional.ofNullable(markedReadTimes.get(repoId).get(issue));
	}

	public void clearMarkedReadAt(String repoId, int issue) {
		if (!markedReadTimes.containsKey(repoId)) {
			// No need to do anything
			return;
		}
		if (markedReadTimes.get(repoId).containsKey(issue)) {
			markedReadTimes.get(repoId).remove(issue);
		}
	}

	public void addBoard(String name, List<String> filterExprs) {
		boards.put(name, filterExprs);
	}

	public List<String> getBoardPanels(String name) {
		return boards.get(name);
	}

	public Map<String, List<String>> getAllBoards() {
		return new HashMap<>(boards);
	}

	public void removeBoard(String name) {
		boards.remove(name);
	}

	public void setLastOpenFilters(List<String> filter) {
		lastOpenFilters = new ArrayList<>(filter);
	}

	public List<String> getLastOpenFilters() {
		return new ArrayList<>(lastOpenFilters);
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
