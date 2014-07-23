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
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ConfigFileHandler {

	private static final String CHARSET = "UTF-8";
	private static final String FILE_CONFIG_SESSION = "session-config.json";
	private static final String FILE_CONFIG_LOCAL = "local-config.json";
	private static final String DIR_CONFIG_PROJECTS = "project-config";
	
	
	private static Gson gson = new GsonBuilder()
								.setPrettyPrinting()
								.excludeFieldsWithModifiers(Modifier.TRANSIENT)
								.registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
									@Override
									public JsonElement serialize(
											LocalDateTime src, Type typeOfSrc,
											JsonSerializationContext context) {
										Instant instant = src.atZone(ZoneId.systemDefault()).toInstant();
										long epochMilli = instant.toEpochMilli();
										return new JsonPrimitive(epochMilli);
									}
									
								})
								.registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
									@Override
									public LocalDateTime deserialize(
											JsonElement json, Type typeOfT,
											JsonDeserializationContext context)
											throws JsonParseException {
										Instant instant = Instant.ofEpochMilli(json.getAsJsonPrimitive().getAsLong());
								        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
									}								
								})
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
			List<String> nonInheritedLabels = new ArrayList<String>();
			nonInheritedLabels.add("status.");
			List<String> openStatusLabels = new ArrayList<String>();
			openStatusLabels.add("status.open");
			List<String> closedStatusLabels = new ArrayList<String>();
			closedStatusLabels.add("status.closed");
			// default project configuration file
			config = new ProjectConfigurations(nonInheritedLabels, openStatusLabels, closedStatusLabels);
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

	public static void saveLocalConfig(LocalConfigurations config) {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(FILE_CONFIG_LOCAL) , CHARSET);
			gson.toJson(config, LocalConfigurations.class, writer);
			writer.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static LocalConfigurations loadLocalConfig() {
		LocalConfigurations config = null;
		File configFile = new File(FILE_CONFIG_LOCAL);
		if (configFile.exists()) {
			try {
				Reader reader = new InputStreamReader(new FileInputStream(FILE_CONFIG_LOCAL), CHARSET);
				config = gson.fromJson(reader, LocalConfigurations.class);
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
				config = new LocalConfigurations();
				saveLocalConfig(config);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return config;
	}
}
