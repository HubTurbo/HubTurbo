package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.eclipse.egit.github.core.IRepositoryIdProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConfigFileHandler {

	private static final String CHARSET = "UTF-8";
	private static final String FILE_CONFIG_SESSION = "session-config.json";
	private static final String DIR_CONFIG_PROJECTS = "project-config";
	
	
	private static Gson gson = new GsonBuilder()
								.setPrettyPrinting()
								.excludeFieldsWithModifiers(Modifier.TRANSIENT)
								.create();
	
	private static void saveProjectConfig(ProjectConfigurations config, IRepositoryIdProvider repoId) {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(generateFileName(repoId)), CHARSET);
			gson.toJson(config, ProjectConfigurations.class, writer);
			writer.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static ProjectConfigurations loadProjectConfig(IRepositoryIdProvider repoId) {
		directorySetup();
		ProjectConfigurations config = null;
		String fileName = generateFileName(repoId);
		File configFile = new File(fileName);
		if (configFile.exists()) {
			try {
				Reader reader = new InputStreamReader(new FileInputStream(fileName), CHARSET);
				config = gson.fromJson(reader, ProjectConfigurations.class);
				reader.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			config = new ProjectConfigurations(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
			try {
				configFile.createNewFile();
				saveProjectConfig(config, repoId);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return config;
	}
	
	private static void directorySetup() {
		File directory = new File(DIR_CONFIG_PROJECTS);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}

	private static String generateFileName(IRepositoryIdProvider repoId) {
		String[] repoIdTokens = repoId.generateId().split("/");
		String fileName = DIR_CONFIG_PROJECTS + File.separator + repoIdTokens[0] + " " + repoIdTokens[1] + ".json";
		return fileName;
	}

	public static void saveSessionConfig(SessionConfigurations config) {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(FILE_CONFIG_SESSION) , CHARSET);
			gson.toJson(config, SessionConfigurations.class, writer);
			writer.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static SessionConfigurations loadSessionConfig() {
		SessionConfigurations config = null;
		File configFile = new File(FILE_CONFIG_SESSION);
		if (configFile.exists()) {
			try {
				Reader reader = new InputStreamReader(new FileInputStream(FILE_CONFIG_SESSION), CHARSET);
				config = gson.fromJson(reader, SessionConfigurations.class);
				reader.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (config == null) config = new SessionConfigurations();
		return config;
	}
	
}
