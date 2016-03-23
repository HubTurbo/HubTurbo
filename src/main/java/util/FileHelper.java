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

    private final String fileDirectory;
    private final String fileName;

    /**
     * Initialises the FileHelper with the directory and the file name
     * @param fileDirectory the directory that the file is located in
     * @param fileName the name of the file
     */
    public FileHelper(String fileDirectory, String fileName) {
        this.fileName = fileName;
        this.fileDirectory = fileDirectory;
    }

    /**
     * Returns the File representation at the file path
     */
    private Path getFilePath() {
        return Paths.get(fileDirectory, fileName);
    }

    /**
     * Attempts to create the directory if it does not exist.
     * @throws IOException When the directory cannot be created
     */
    private void createDirectoryIfNonExistent() throws IOException {
        File directory = new File(fileDirectory);
        boolean directoryExists = directory.exists() && directory.isDirectory();
        if (!directoryExists) {
            if (directory.mkdirs()) {
                logger.info("Directory created: " + directory.toString());
            } else {
                logger.error("Could not create file directory");
                throw new IOException();
            }
        }
    }

    public boolean exists() {
        return Files.exists(getFilePath());
    }

    /**
     * Loads the contents of the file into a String
     * @return The String representation of the contents of the file
     */
    public String loadFileContents() throws IOException {
        Path filePath = getFilePath();
        logger.info("Load starting: " + filePath.toAbsolutePath());
        String contents = new String(Files.readAllBytes(filePath), CHARSET);
        logger.info("Load successful: " + filePath.toAbsolutePath());
        return contents;
    }

    /**
     * Writes a String to the file
     * @param fileContents The contents of the file in String form, to be written
     */
    public void writeFileContents(String fileContents) throws IOException {
        createDirectoryIfNonExistent();
        Path filePath = getFilePath();
        logger.info("Write starting: " + filePath.toAbsolutePath());
        Files.write(filePath, fileContents.getBytes(CHARSET));
        logger.info("Write successful: " + filePath.toAbsolutePath());
    }

}
