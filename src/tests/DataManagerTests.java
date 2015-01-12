package tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.egit.github.core.RepositoryId;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import storage.ConfigFileHandler;
import storage.DataManager;

public class DataManagerTests {

	private static final String FILE_CONFIG_SESSION = "session-config-test.json";
	private static final String FILE_CONFIG_LOCAL = "local-config-test.json";
	private static final String DIR_CONFIG_PROJECTS = ".hubturboconfig-test";
	
	private static DataManager dataManager;
	private static ConfigFileHandler stubFileHandler;

	@BeforeClass
	public static void setup() {
		dataManager = DataManager.getInstance();
		stubFileHandler = new ConfigFileHandler(FILE_CONFIG_SESSION, FILE_CONFIG_LOCAL, DIR_CONFIG_PROJECTS);

		dataManager.setConfigFileHandler(stubFileHandler);
	}

	@AfterClass
	public static void tearDown() {
		cleanup();
	}

	@Test
	public void basics() {
		// Saving and loading of empty configuration
		dataManager.saveLocalConfig();
		dataManager.saveSessionConfig();
		reload();
	}
	
	@Test
	public void session() {
		List<String> filters = Arrays.asList(new String[] {"assignee:a milestone:b", "id:1 updated:<24"});
		String lastLogin = "testertest";
		RepositoryId repo = new RepositoryId("hubturbo", "hubturbo");

		dataManager.setFiltersForNextSession(repo, filters);
		dataManager.setLastLoginUsername(lastLogin);

		dataManager.saveSessionConfig();
		reload();
		
		List<String> loadedFilters = dataManager.getFiltersFromPreviousSession(repo);
		String loadedLogin = dataManager.getLastLoginUsername();
		assertEquals(filters, loadedFilters);
		assertEquals(lastLogin, loadedLogin);
	}
	
	/**
	 * Causes DataManager to reload configuration files.
	 */
	private void reload() {
		// Sets the file handler again, which causes the side effect of reloading data files.
		dataManager.setConfigFileHandler(stubFileHandler);
	}

	/**
	 * Deletes all files created. Fails silently so can be called whenever.
	 */
	private static void cleanup() {
		FileUtils.deleteQuietly(new File(FILE_CONFIG_LOCAL));
		FileUtils.deleteQuietly(new File(FILE_CONFIG_SESSION));
		try {
			FileUtils.deleteDirectory(new File(DIR_CONFIG_PROJECTS));
		} catch (IOException e) {
		}
	}
}
