package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConfigFileHandler {

	private static final String FILE_CONFIG = "config.json";
	
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	public void saveConfig(UserConfigurations config) {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(FILE_CONFIG) , "UTF-8");
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
	
	public UserConfigurations loadConfig() {
		UserConfigurations config = null;
		try {
			Reader reader = new InputStreamReader(new FileInputStream(FILE_CONFIG), "UTF-8");
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
		return config;
	}
	
}
