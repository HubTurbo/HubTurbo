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
public class FileHelper {
    private static final Logger logger = LogManager.getLogger(FileHelper.class.getName());

    private static final String CHARSET = "UTF-8";
    private static final String EMPTY_STRING = "";

    private final String fileDirectory;
    private final String fileName;

    /**
     * Initialise the FileHelper with the directory and the file name
     * @param fileDirectory the directory that the config file is located in
     * @param fileName the name of the config file
     */
    public FileHelper(String fileDirectory, String fileName) {
        this.fileName = fileName;
        this.fileDirectory = fileDirectory;
    }

    /**
     * Returns the File representation at the config file path
     */
    private Path getFilePath() {
        return Paths.get(fileDirectory, fileName);
    }

    /**
     * Attempts to create the config directory if it does not exist.
     */
    private void createDirectoryIfNonExistent() throws IOException {
        File directory = new File(fileDirectory);
        boolean directoryExists = directory.exists() && directory.isDirectory();
        if (!directoryExists) {
            if (directory.mkdirs()) {
                logger.info("Config directory created: " + directory.toString());
            } else {
                logger.error("Could not create config file directory");
                throw new IOException();
            }
        }
    }

    /**
     * Loads the content of a new file
     * @return The String representation of the contents in a new file
     */
    public String loadNewFile() {
        return EMPTY_STRING;
    }

    /**
     * Load the contents of the file into a String
     * @return The String representation of the contents of the config file
     */
    public String loadFileContents() throws IOException {
        Path configFilePath = getFilePath();
        logger.info("Load starting: " + configFilePath.toAbsolutePath());

        String contents = new String(Files.readAllBytes(configFilePath), CHARSET);
        logger.info("Load successful: " + configFilePath.toAbsolutePath());
        return contents;
    }

    /**
     * Writes a String to the file
     * @param fileContents The contents of the file in String form, to be written
     */
    public void writeFileContents(String fileContents) throws IOException {
        createDirectoryIfNonExistent();
        Path configFilePath = getFilePath();
        logger.info("Write starting: " + configFilePath.toAbsolutePath());
        Files.write(configFilePath, fileContents.getBytes(CHARSET));
        logger.info("Write successful: " + configFilePath.toAbsolutePath());
    }

}
