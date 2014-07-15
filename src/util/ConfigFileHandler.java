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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConfigFileHandler {

	private static final String CHARSET = "UTF-8";
	private static final String FILE_CONFIG = "config.json";
	
	private static Gson gson = new GsonBuilder()
								.setPrettyPrinting()
								.excludeFieldsWithModifiers(Modifier.TRANSIENT)
								.create();
	
	private static void saveConfig(UserConfigurations config) {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(FILE_CONFIG) , CHARSET);
			gson.toJson(config, UserConfigurations.class, writer);
			writer.close();
		} catch (UnsupportedEncodingException e) {
			// from construction of OutputStreamWriter
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// from construction of FileOutputStream
			e.printStackTrace();
		} catch (IOException e) {
			// from closing writer
			e.printStackTrace();
		}
	}
	
	public static UserConfigurations loadConfig() {
		UserConfigurations config = null;
		File configFile = new File(FILE_CONFIG);
		if (configFile.exists()) {
			try {
				Reader reader = new InputStreamReader(new FileInputStream(FILE_CONFIG), CHARSET);
				config = gson.fromJson(reader, UserConfigurations.class);
				reader.close();
			} catch (UnsupportedEncodingException e) {
				// from construction of InputStreamReader
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// from construction of FileInputStream;
				e.printStackTrace();
			} catch (IOException e) {
				// TODO from closing reader
				e.printStackTrace();
			}
		} else {
			config = new UserConfigurations(Defaults.getDefaultNonInheritedLabels(),
					Defaults.getDefaultOpenStatusLabels(),
					Defaults.getDefaultClosedStatusLabels());
			try {
				configFile.createNewFile();
				saveConfig(config);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return config;
	}
	
}
