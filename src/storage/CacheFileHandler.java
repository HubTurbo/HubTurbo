package storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CacheFileHandler {

	private static final Logger logger = LogManager.getLogger(CacheFileHandler.class.getName());
	private static final String DIR_CACHE = ".hubturbocache";
	private static final String FILE_DATA_CACHE = "-cache.json";
	private static final String FILE_DATA_CACHE_TEMP = "-cache-temp.json";

	private CachedRepoData repo = null;
	private String repoId = null;

	public CacheFileHandler(String repoId) {
		this.repoId = repoId;
		directorySetup();
		readFromFile();
	}

	private void directorySetup() {
		File directory = new File(DIR_CACHE);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}

	public void readFromFile() {
		Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();

		try {
			BufferedReader bufferedReader = new BufferedReader(
					new FileReader(getFileName(FILE_DATA_CACHE, this.repoId)));

			repo = gson.fromJson(bufferedReader, CachedRepoData.class);

			bufferedReader.close();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	private String getFileName(String givenFileName, String repoIdString) {
		String[] repoIdTokens = repoIdString.split("/");
		String repoFileName = repoIdTokens[0] + "_" + repoIdTokens[1];
		return DIR_CACHE + File.separator + repoFileName + givenFileName;
	}

	public CachedRepoData getRepo() {
		return repo;
	}

	public void writeToFile(String repoIdString, String issuesETag, String collabsETag, String labelsETag,
			String milestonesETag, String issueCheckTime, List<TurboUser> collaborators, List<TurboLabel> labels,
			List<TurboMilestone> milestones, List<TurboIssue> issues) {

		logger.info("About to write to file for repo: " + repoIdString + " with last checked time: " + issueCheckTime);

		CachedRepoData currentRepoData = new CachedRepoData(issuesETag, collabsETag, labelsETag, milestonesETag,
				issueCheckTime, new ArrayList<>(collaborators), new ArrayList<>(labels), new ArrayList<>(milestones),
				new ArrayList<>(issues));

		Gson gson = new GsonBuilder().setPrettyPrinting()
				.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();

		String json = gson.toJson(currentRepoData);

		// Save to temp file first, then replace main cache file when done
		try {
			FileWriter writer = new FileWriter(getFileName(FILE_DATA_CACHE_TEMP, repoIdString));
			writer.write(json);
			writer.close();
			logger.info("Done writing to file for repo: " + repoIdString);

			File file = new File(getFileName(FILE_DATA_CACHE, repoIdString));

			if (file.exists() && !file.delete()) {
				logger.error("Failed to delete cache file");
			}

			File newFile = new File(getFileName(FILE_DATA_CACHE_TEMP, repoIdString));
			if (!newFile.renameTo(file)) {
				logger.error("Failed to rename temp cache file");
			}
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
}
