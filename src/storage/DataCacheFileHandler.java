package storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DataCacheFileHandler {

	private static final Logger logger = LogManager.getLogger(DataCacheFileHandler.class.getName());
	private static final String DIR_CACHE = ".hubturbocache";
	private static final String FILE_DATA_CACHE = "-cache.json";
	private static final String FILE_DATA_CACHE_TEMP = "-cache-temp.json";

	private TurboRepoData repo = null;
	private String repoId = null;
	
	public DataCacheFileHandler(String repoId) {
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
		Gson gson = new GsonBuilder()
			.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
			.create();

		String filename = getFileName(FILE_DATA_CACHE, this.repoId);
		if (new File(filename).exists()) {
			try {
				BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
				repo = gson.fromJson(bufferedReader, TurboRepoData.class);
				bufferedReader.close();
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}
	}
	
	private String getFileName(String givenFileName, String repoIdString) {
		String[] repoIdTokens = repoIdString.split("/");
		String repoFileName = repoIdTokens[0] + "_" + repoIdTokens[1];
		return DIR_CACHE + File.separator + repoFileName + givenFileName;
	}
	
	public TurboRepoData getRepo() {
		return repo;
	}

	public void writeToFile(String repoIdString, String issuesETag, String collabsETag, String labelsETag, String milestonesETag, String issueCheckTime, List<TurboUser> collaborators, List<TurboLabel> labels, List<TurboMilestone> milestones, List<TurboIssue> issues) {
		logger.info("About to write to file for repo: " + repoIdString + " with last checked time: " + issueCheckTime);
		
		TurboRepoData currentRepoData = new TurboRepoData(issuesETag, collabsETag, labelsETag, milestonesETag, issueCheckTime, collaborators, labels, milestones, issues);

		Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
			.create();

		String json = gson.toJson(currentRepoData);
		
		// Save to temp file first to mitigate corruption of data. Once writing is done, rename it to main cache file
		try {
			FileWriter writer = new FileWriter(getFileName(FILE_DATA_CACHE_TEMP, repoIdString));
			writer.write(json);
			writer.close();
			logger.info("Done writing to file for repo: " + repoIdString);
			
			String filename = getFileName(FILE_DATA_CACHE, repoIdString);
			File file = new File(filename);
			
			if (file.exists() && !file.delete()) {
				logger.error("Failed to delete cache file " + filename);
			} 
			
			File newFile = new File(getFileName(FILE_DATA_CACHE_TEMP, repoIdString));
			if (!newFile.renameTo(file)) {
				logger.error("Failed to rename temp cache file " + filename);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
