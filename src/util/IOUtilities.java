package util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

import ui.UI;

public class IOUtilities {
	public static ByteArrayOutputStream inputStreamToByteArrayOutputStream(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[4096];
		while ((nRead = is.read(data, 0, data.length)) != -1) {
		  buffer.write(data, 0, nRead);
		}
		buffer.flush();
		return buffer;
	}
	
	public static Optional<String> readResource(String packagePath) {
		ClassLoader classLoader = UI.class.getClassLoader();
		BufferedReader reader;
		InputStream inputStream = classLoader.getResourceAsStream(packagePath);
		StringBuilder sb = new StringBuilder();
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			reader.close();
			return Optional.of(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}
}
