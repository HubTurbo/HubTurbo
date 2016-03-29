package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A helper that manages file I/O.
 */
public final class FileHelper {
    private static final Logger logger = LogManager.getLogger(FileHelper.class.getName());

    private static final String CHARSET = "UTF-8";

    /**
     * Private constructor to prevent instantiation of utility class
     */
    private FileHelper() {
    }

    /**
     * Returns the File representation at the file path
     */
    private static Path getFilePath(String fileDirectory, String fileName) {
        return Paths.get(fileDirectory, fileName);
    }

    /**
     * Attempts to create the directory if it does not exist.
     * @throws IOException When the directory cannot be created
     */
    private static void createDirectoryIfNonExistent(String fileDirectory) throws IOException {
        File directory = new File(fileDirectory);
        boolean isDirectoryExists = directory.exists() && directory.isDirectory();
        if (isDirectoryExists) {
            return;
        }
        boolean isMkdirsSuccessful = directory.mkdirs();
        if (isMkdirsSuccessful) {
            logger.info("Directory created: " + directory.toString());
            return;
        }
        logger.error("Could not create file directory");
        throw new IOException("Could not create file directory");
    }

    public static boolean isFileExists(String fileDirectory, String fileName) {
        return Files.exists(getFilePath(fileDirectory, fileName));
    }

    /**
     * Loads the contents of the file into a String
     * @return The String representation of the contents of the file
     */
    public static String getFileContents(String fileDirectory, String fileName) throws IOException {
        Path filePath = getFilePath(fileDirectory, fileName);
        logger.info("Load starting: " + filePath.toAbsolutePath());
        String contents = new String(Files.readAllBytes(filePath), CHARSET);
        logger.info("Load successful: " + filePath.toAbsolutePath());
        return contents;
    }

    /**
     * Writes a String to the file
     * @param fileContents The contents of the file in String form, to be written
     */
    public static void writeFileContents(String fileDirectory, String fileName, String fileContents)
            throws IOException {
        createDirectoryIfNonExistent(fileDirectory);
        Path filePath = getFilePath(fileDirectory, fileName);
        logger.info("Write starting: " + filePath.toAbsolutePath());
        Files.write(filePath, fileContents.getBytes(CHARSET));
        logger.info("Write successful: " + filePath.toAbsolutePath());
    }

}
