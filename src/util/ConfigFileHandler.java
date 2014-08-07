package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
	private static final Logger logger = LogManager.getLogger(ConfigFileHandler.class.getName());
	private static final String GITHUB_DOMAIN = "https://raw.githubusercontent.com/";
	private static final String ADDRESS_SEPARATOR = "/";
	private static final String DEFAULT_BRANCH = "master";
	
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
		
		// Download config file from repo if available
		if (isValidURL(generateFileURL(repoId))) {
			try {
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

	private static void directorySetup() {
		File directory = new File(DIR_CONFIG_PROJECTS);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}

	private static String generateFileName(IRepositoryIdProvider repoId) {
		String[] repoIdTokens = repoId.generateId().split("/");
		String fileName = DIR_CONFIG_PROJECTS + File.separator + repoIdTokens[0].toLowerCase() + " " + repoIdTokens[1].toLowerCase() + ".json";
		return fileName;
	}
	
	private static String generateFileURL(IRepositoryIdProvider repoId) {
		String[] repoIdTokens = repoId.generateId().split(ADDRESS_SEPARATOR);
		String stringURL = GITHUB_DOMAIN + repoIdTokens[0] 
									     + ADDRESS_SEPARATOR + repoIdTokens[1]
									     + ADDRESS_SEPARATOR + DEFAULT_BRANCH
									     + ADDRESS_SEPARATOR + DIR_CONFIG_PROJECTS
									     + ADDRESS_SEPARATOR + repoIdTokens[0].toLowerCase() + " " + repoIdTokens[1].toLowerCase() + ".json";
		return stringURL;
	}

	private static boolean isValidURL(String stringURL) {
		HttpURLConnection httpUrlConn;
		try {
			httpUrlConn = (HttpURLConnection) new URL(stringURL)
			.openConnection();

			// A HEAD request is just like a GET request, except that it asks
			// the server to return the response headers only, and not the
			// actual resource (i.e. no message body).
			// This is useful to check characteristics of a resource without
			// actually downloading it,thus saving bandwidth. Use HEAD when
			// you don't actually need a file's contents.
			httpUrlConn.setRequestMethod("HEAD");

			// Set timeouts in milliseconds
			httpUrlConn.setConnectTimeout(30000);
			httpUrlConn.setReadTimeout(30000);

			return (httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void download(String stringURL, String destination) throws IOException {
		// File name that is being downloaded
		String downloadedFileName = stringURL.substring(stringURL.lastIndexOf(ADDRESS_SEPARATOR) + 1);
		// Converts the input string to a Path object.
        Path inputPath = Paths.get(destination);
        
		// Open connection to the file
		URL url = new URL(stringURL);
		InputStream inStream = url.openStream();
		// Stream to the destination file
		FileOutputStream fos = new FileOutputStream(inputPath.toAbsolutePath().toString());

		// Read bytes from URL to the local file
		byte[] buffer = new byte[4096];
		int bytesRead = 0;

		System.out.print("Downloading " + downloadedFileName);
		while ((bytesRead = inStream.read(buffer)) != -1) {
			System.out.print(".");	// Progress bar
			fos.write(buffer,0,bytesRead);
		}
		System.out.println("done!");

		// Close destination stream
		fos.close();
		// Close URL stream
		inStream.close();
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
