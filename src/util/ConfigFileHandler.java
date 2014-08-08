package util;

import java.io.BufferedInputStream;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryContents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import service.ServiceManager;

public class ConfigFileHandler {

	private static final String CHARSET = "UTF-8";
	private static final String FILE_CONFIG_SESSION = "session-config.json";
	private static final String FILE_CONFIG_LOCAL = "local-config.json";
	private static final String DIR_CONFIG_PROJECTS = ".hubturboconfig";
	private static final String ADDRESS_SEPARATOR = "/";
	private static final String URL_SPACE = "%20";
	private static final int BUFFER_SIZE = 1024;
	private static final String GITHUB_DOMAIN = "https://raw.githubusercontent.com";
	private static final String DEFAULT_BRANCH = "master";
	private static final Logger logger = LogManager.getLogger(ConfigFileHandler.class.getName());
	
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
			logger.error(e.getLocalizedMessage(), e);
		} catch (FileNotFoundException e) {
			logger.error(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
	
	public static ProjectConfigurations loadProjectConfig(IRepositoryIdProvider repoId) {
		directorySetup();
		ProjectConfigurations config = null;
		String fileName = generateFileName(repoId);

		if (isValidURL(generateFileURL(repoId))) {
			try {
				// Download config file from repo if available
				download(generateFileURL(repoId), fileName);
				File configFile = new File(fileName);
				if (configFile.exists()) {
					config = readConfigFile(fileName);
				} else {
					config = createConfigFile(repoId, fileName);
				}
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		
		} else {
			config = createConfigFile(repoId, fileName);
		}
		
		return config;
	}

	private static ProjectConfigurations readConfigFile(String fileName) {
		ProjectConfigurations config = null;
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
		return config;
	}

	private static ProjectConfigurations createConfigFile(IRepositoryIdProvider repoId, String fileName) {
		ProjectConfigurations config = null;
		List<String> nonInheritedLabels = new ArrayList<String>();
		nonInheritedLabels.add("status.");
		List<String> openStatusLabels = new ArrayList<String>();
		openStatusLabels.add("status.open");
		List<String> closedStatusLabels = new ArrayList<String>();
		closedStatusLabels.add("status.closed");
		// Default project configuration file
		config = new ProjectConfigurations(nonInheritedLabels, openStatusLabels, closedStatusLabels);
		File configFile = new File(fileName);
		try {
			configFile.createNewFile();
			saveProjectConfig(config, repoId);
		} catch (IOException e) {
			e.printStackTrace();
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
		String fileName = DIR_CONFIG_PROJECTS + File.separator + determineConfigFileName(repoId, " ");
		return fileName;
	}

	private static String determineConfigFileName(IRepositoryIdProvider repoId, String space_char) {
		String[] repoIdTokens = repoId.generateId().split("/");
		String expectedFileName = repoIdTokens[0] + space_char + repoIdTokens[1] + ".json";
		String configFileName = expectedFileName;
		try {
			ServiceManager service = ServiceManager.getInstance();
			List<RepositoryContents> repoContents = service.getContents(repoId, DIR_CONFIG_PROJECTS);
			for (RepositoryContents content : repoContents) {
				if (content.getName().equalsIgnoreCase(expectedFileName)) {
					configFileName = content.getName();
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return configFileName;
	}

	private static String generateFileURL(IRepositoryIdProvider repoId) {
		String[] repoIdTokens = repoId.generateId().split(ADDRESS_SEPARATOR);
		String urlString = GITHUB_DOMAIN + ADDRESS_SEPARATOR + repoIdTokens[0] 
										 + ADDRESS_SEPARATOR + repoIdTokens[1]
										 + ADDRESS_SEPARATOR + DEFAULT_BRANCH
										 + ADDRESS_SEPARATOR + DIR_CONFIG_PROJECTS
										 + ADDRESS_SEPARATOR + determineConfigFileName(repoId, URL_SPACE);
		return urlString;
	}

	private static boolean isValidURL(String stringURL) {
		HttpURLConnection httpUrlConn;
		try {
			httpUrlConn = (HttpURLConnection) new URL(stringURL)
			.openConnection();

			// Check if resource is available without downloading it
			httpUrlConn.setRequestMethod("HEAD");
			httpUrlConn.setConnectTimeout(30000);
			httpUrlConn.setReadTimeout(30000);

			return (httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void download(String stringURL, String destination) throws IOException {
		String downloadedFileName = stringURL.substring(stringURL.lastIndexOf(ADDRESS_SEPARATOR) + 1);
        Path inputPath = Paths.get(destination);
        
		URL url = new URL(stringURL);
		BufferedInputStream inStream  = null;
		FileOutputStream fos = null;
		try {
			inStream = new BufferedInputStream(url.openStream());
			fos = new FileOutputStream(inputPath.toAbsolutePath().toString());

			byte data[] = new byte[BUFFER_SIZE];
			int bytesRead;
			System.out.print("Downloading " + downloadedFileName);
			while ((bytesRead = inStream.read(data, 0, BUFFER_SIZE)) != -1) {
				System.out.print(".");	// Progress bar
				fos.write(data, 0, bytesRead);
			}
			System.out.println("done!");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inStream != null)
				inStream.close();
			if (fos != null)
				fos.close();
		}
	}

	public static void saveSessionConfig(SessionConfigurations config) {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(FILE_CONFIG_SESSION) , CHARSET);
			gson.toJson(config, SessionConfigurations.class, writer);
			writer.close();
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getLocalizedMessage(), e);
		} catch (FileNotFoundException e) {
			logger.error(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
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
				logger.error(e.getLocalizedMessage(), e);
			} catch (FileNotFoundException e) {
				logger.error(e.getLocalizedMessage(), e);
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		} else {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage(), e);
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
			logger.error(e.getLocalizedMessage(), e);
		} catch (FileNotFoundException e) {
			logger.error(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
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
				logger.error(e.getLocalizedMessage(), e);
			} catch (FileNotFoundException e) {
				logger.error(e.getLocalizedMessage(), e);
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		} else {
			try {
				configFile.createNewFile();
				config = new LocalConfigurations();
				saveLocalConfig(config);
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}
		return config;
	}
}
