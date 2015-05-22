package prefs;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.io.*;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ConfigFileHandler {

	private static final Logger logger = LogManager.getLogger(ConfigFileHandler.class.getName());

	private static final String CHARSET = "UTF-8";
	private final String globalConfigFileName;
	private final String configDirectory;

	private Gson gson;

	public ConfigFileHandler(String configDirectory, String globalConfigFileName) {
		this.globalConfigFileName = globalConfigFileName;
		this.configDirectory = configDirectory;

		setupGson();
		ensureDirectoryExists();
	}

	private void ensureDirectoryExists() {
		File directory = new File(configDirectory);
		if (!directory.exists() || !directory.isDirectory()) {
			directory.mkdirs();
		}
	}

	private File getGlobalConfigFile() {
		return new File(configDirectory, globalConfigFileName).getAbsoluteFile();
	}

	public void saveGlobalConfig(GlobalConfig config) {
		File configFile = getGlobalConfigFile();
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(configFile) , CHARSET);
			gson.toJson(config, GlobalConfig.class, writer);
			writer.close();
		} catch (IOException e) {
			HTLog.error(logger, e);
		}
	}

	public GlobalConfig loadGlobalConfig() {
		
		// Default to an empty configuration
		GlobalConfig config = new GlobalConfig();
		
		File configFile = getGlobalConfigFile();
		if (configFile.exists()) {
			try {
				Reader reader = new InputStreamReader(new FileInputStream(configFile), CHARSET);
				config = gson.fromJson(reader, GlobalConfig.class);
				reader.close();
			} catch (IOException e) {
				HTLog.error(logger, e);
			}
		} else {
			try {
				configFile.createNewFile();
				saveGlobalConfig(config);
			} catch (IOException e) {
				HTLog.error(logger, e);
			}
		}
		return config;
	}

	private void setupGson() {
		 gson = new GsonBuilder()
			.setPrettyPrinting()
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
			.registerTypeAdapter(LocalDateTime.class,
				(JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
					Instant instant = Instant.ofEpochMilli(json.getAsJsonPrimitive().getAsLong());
					return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
				}
			).create();
	}
}