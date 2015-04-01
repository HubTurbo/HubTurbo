package storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private static final Logger logger = LogManager.getLogger(ConfigFileHandler.class.getName());
	
	private Gson gson;

	private final String sessionConfigFilePath;
	private final String localConfigFilePath;

	public ConfigFileHandler(String sessionConfigFilePath, String localConfigFilePath) {

		this.localConfigFilePath = localConfigFilePath;
		this.sessionConfigFilePath = sessionConfigFilePath;

		setupGson();
	}

	/**
	 * Local and session configuration
	 */
	
	/**
	 * Writes to the session configuration file.
	 */
	public void saveSessionConfig(SessionConfiguration config) {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(sessionConfigFilePath) , CHARSET);
			gson.toJson(config, SessionConfiguration.class, writer);
			writer.close();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Locals session configuration file, creating it if it doesn't exist.
	 */
	public SessionConfiguration loadSessionConfig() {
		
		// Default to an empty configuration
		SessionConfiguration config = new SessionConfiguration();
		
		File configFile = new File(sessionConfigFilePath);
		if (configFile.exists()) {
			try {
				Reader reader = new InputStreamReader(new FileInputStream(sessionConfigFilePath), CHARSET);
				config = gson.fromJson(reader, SessionConfiguration.class);
				reader.close();
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		} else {
			try {
				configFile.createNewFile();
				saveSessionConfig(config);
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}
		return config;
	}

	/**
	 * Writes to the local configuration file.
	 */
	public void saveLocalConfig(LocalConfiguration config) {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(localConfigFilePath), CHARSET);
			gson.toJson(config, LocalConfiguration.class, writer);
			writer.close();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * Locals local configuration file, creating it if it doesn't exist.
	 */
	public LocalConfiguration loadLocalConfig() {
		
		// Default to an empty configuration
		LocalConfiguration config = new LocalConfiguration();
		
		File configFile = new File(localConfigFilePath);
		if (configFile.exists()) {
			try {
				Reader reader = new InputStreamReader(new FileInputStream(localConfigFilePath), CHARSET);
				config = gson.fromJson(reader, LocalConfiguration.class);
				reader.close();
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		} else {
			try {
				configFile.createNewFile();
				saveLocalConfig(config);
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage(), e);
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
	}
}