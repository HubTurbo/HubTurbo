package storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.ObservableList;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DataCacheFileHandler {

	private static final Logger logger = LogManager.getLogger(DataCacheFileHandler.class.getName());
	private static final String DIR_CACHE = ".hubturbocache";
	private static final String FILE_DATA_CACHE = "-cache.json";
	private static final String FILE_DATA_CACHE_TEMP = "-cache-temp.json";

	private List<TurboUser> collaborators = null;
	private List<TurboLabel> labels = null;
	private List<TurboMilestone> milestones = null;
	private List<TurboIssue> issues = null;
	
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
		Gson gson = new Gson();
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(getFileName(FILE_DATA_CACHE, this.repoId)));
			
			repo = gson.fromJson(bufferedReader, TurboRepoData.class);
			
			bufferedReader.close();
		} catch (FileNotFoundException e){
			
		} catch (IOException e) {
			e.printStackTrace();
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

	public void setCollaborators(List<TurboUser> collaborators) {
		this.collaborators = collaborators;
	}
	
	public void setLabels(List<TurboLabel> labels) {
		this.labels = labels;
	}
	
	public void setMilestones(List<TurboMilestone> milestones) {
		this.milestones = milestones;
	}
	
	public void setIssues(List<TurboIssue> issues) {
		this.issues = issues;
	}
	
	public void writeToFile(String repoIdString, String issuesETag, String collabsETag, String labelsETag, String milestonesETag, String issueCheckTime, ObservableList<TurboUser> collaborators, ObservableList<TurboLabel> labels, ObservableList<TurboMilestone> milestones, ObservableList<TurboIssue> issues) {
		logger.info("About to write to file...");
		this.issues = issues.stream().collect(Collectors.toList());
		this.collaborators = collaborators.stream().collect(Collectors.toList());
		this.labels = labels.stream().collect(Collectors.toList());
		this.milestones = milestones.stream().collect(Collectors.toList());
		
		TurboRepoData currentRepoData = new TurboRepoData(issuesETag, collabsETag, labelsETag, milestonesETag, issueCheckTime, this.collaborators, this.labels, this.milestones, this.issues);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(currentRepoData);
		
		// Save to temp file first to mitigate corruption of data. Once writing is done, rename it to main cache file
		try {
			FileWriter writer = new FileWriter(getFileName(FILE_DATA_CACHE_TEMP, repoIdString));
			writer.write(json);
			writer.close();
			logger.info("Done writing to file");
			
			File file = new File(getFileName(FILE_DATA_CACHE, repoIdString));
			
			if (file.exists()) {
				if (file.delete()) {
					//System.out.println("Cache file is deleted");
				} else {
					logger.error("Failed to delete cache file");
				}
			} 
			
			File newFile = new File(getFileName(FILE_DATA_CACHE_TEMP, repoIdString));
			if (newFile.renameTo(file)) {
				//System.out.println("Temp cache file is renamed!");
			} else {
				logger.error("Failed to rename temp cache file");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
