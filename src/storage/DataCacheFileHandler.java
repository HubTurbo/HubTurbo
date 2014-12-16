package storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class DataCacheFileHandler {

	private static DataCacheFileHandler dataCacheFileHandlerInstance = null;
	private static final String FILE_DATA_CACHE = "data-cache.json";
	private static final String FILE_DATA_CACHE_TEMP = "data-cache-temp.json";

	private List<TurboUser> collaborators = null;
	private List<TurboLabel> labels = null;
	private List<TurboMilestone> milestones = null;
	private List<TurboIssue> issues = null;
	private List<TurboRepoData> repoDataList = null;	
	
	private DataCacheFileHandler() {
		readFromFile();
	}
	
	public static DataCacheFileHandler getInstance() {
		if (dataCacheFileHandlerInstance == null) {
			dataCacheFileHandlerInstance = new DataCacheFileHandler();
		}
		return dataCacheFileHandlerInstance;
	}

	public void readFromFile() {
		Gson gson = new Gson();
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(FILE_DATA_CACHE));

			repoDataList = gson.fromJson(bufferedReader, new TypeToken<List<TurboRepoData>>(){}.getType());
		} catch (FileNotFoundException e){
			
		}
	}
	
	public TurboRepoData getRepoGivenId(String repoId) {
		if (repoDataList != null) {
			for (TurboRepoData repo : repoDataList) {
				if (repo.getRepoId().equals(repoId)) {
					return repo;
				}
			}
		}
		return null;
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
	
	@SuppressWarnings("unused")
	public void writeToFile(String repoId, String issuesETag, String collabsETag, String labelsETag, String milestonesETag, ObservableList<TurboUser> collaborators, ObservableList<TurboLabel> labels, ObservableList<TurboMilestone> milestones, ObservableList<TurboIssue> issues) {
		System.out.println("Writing to file...");
		this.issues = issues.stream().collect(Collectors.toList());
		this.collaborators = collaborators.stream().collect(Collectors.toList());
		this.labels = labels.stream().collect(Collectors.toList());
		this.milestones = milestones.stream().collect(Collectors.toList());
		
		TurboRepoData currentRepoData = new TurboRepoData(repoId, issuesETag, collabsETag, labelsETag, milestonesETag, this.collaborators, this.labels, this.milestones, this.issues);
		if (repoDataList == null) {
			repoDataList = new ArrayList<TurboRepoData>();
		} else {
			for (int i = 0; i < repoDataList.size(); i++) {
				if (repoDataList.get(i).getRepoId().equals(repoId)) {
					repoDataList.remove(i);
					break;
				}
			}
		}
		repoDataList.add(currentRepoData);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(repoDataList);
		try {
			FileWriter writer = new FileWriter(FILE_DATA_CACHE_TEMP);
			writer.write(json);
			writer.close();
			
			File file = new File(FILE_DATA_CACHE);
			
			if (file.exists()) {
				boolean deleted = file.delete();
			} 
			
			File newFile = new File(FILE_DATA_CACHE_TEMP);
			boolean renamed = newFile.renameTo(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
