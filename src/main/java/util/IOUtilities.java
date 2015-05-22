package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.UI;

import java.io.*;
import java.util.Optional;

public class IOUtilities {

	private static final Logger logger = LogManager.getLogger(IOUtilities.class.getName());

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
			logger.error(e.getLocalizedMessage(), e);
		}
		return Optional.empty();
	}
}
