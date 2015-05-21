//package tests;
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.File;
//import java.util.Arrays;
//import java.util.List;
//
//import org.apache.commons.io.FileUtils;
//import org.eclipse.egit.github.core.RepositoryId;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import service.ServiceManager;
//import storage.ConfigFileHandler;
//import storage.DataManager;
//
//public class DataManagerTests {
//
//	private static final String FILE_CONFIG_SESSION = "session-config-test.json";
//	private static final String FILE_CONFIG_LOCAL = "local-config-test.json";
//
//	private static DataManager dataManager;
//	private static ConfigFileHandler stubFileHandler;
//
//	@BeforeClass
//	public static void setup() {
//
//		ServiceManager.isInTestMode = true;
//
//		dataManager = DataManager.getInstance();
//		stubFileHandler = new ConfigFileHandler(FILE_CONFIG_SESSION, FILE_CONFIG_LOCAL);
//
//		dataManager.setConfigFileHandler(stubFileHandler);
//	}
//
//	@AfterClass
//	public static void tearDown() {
//		cleanup();
//	}
//
//	@Test
//	public void basics() {
//		// Saving and loading of empty configuration
//		dataManager.saveLocalConfig();
//		dataManager.saveSessionConfig();
//		reload();
//	}
//
//	@Test
//	public void session() {
//		List<String> filters = Arrays.asList(new String[] {"assignee:a milestone:b", "id:1 updated:<24"});
//		String lastLogin = "testertest";
//		RepositoryId repo = new RepositoryId("hubturbo", "hubturbo");
//
//		dataManager.setLastOpenFilters(repo, filters);
//		dataManager.setLastLoginUsername(lastLogin);
//
//		dataManager.saveSessionConfig();
//		reload();
//
//		List<String> loadedFilters = dataManager.getLastOpenFilters(repo);
//		String loadedLogin = dataManager.getLastLoginUsername();
//		assertEquals(filters, loadedFilters);
//		assertEquals(lastLogin, loadedLogin);
//	}
//
//	@Test
//	public void local() {
//		List<String> savedSet1 = Arrays.asList(new String[] {"assignee:a milestone:b", "id:1 updated:<24"});
//		List<String> savedSet2 = Arrays.asList(new String[] {"some syntax", "id:2 & id:3"});
//
//		dataManager.addBoard("aaa", savedSet1);
//		dataManager.addBoard("bbb", savedSet2);
//
//		dataManager.saveLocalConfig();
//		reload();
//
//		List<String> loadedSet1 = dataManager.getBoardPanels("aaa");
//		List<String> loadedSet2 = dataManager.getBoardPanels("bbb");
//		assertEquals(savedSet1, loadedSet1);
//		assertEquals(savedSet2, loadedSet2);
//	}
//
//	/**
//	 * Causes DataManager to reload configuration files.
//	 */
//	private void reload() {
//		// Sets the file handler again, which causes the side effect of reloading data files.
//		dataManager.setConfigFileHandler(stubFileHandler);
//	}
//
//	/**
//	 * Deletes all files created. Fails silently so can be called whenever.
//	 */
//	private static void cleanup() {
//		FileUtils.deleteQuietly(new File(FILE_CONFIG_LOCAL));
//		FileUtils.deleteQuietly(new File(FILE_CONFIG_SESSION));
//	}
//}
