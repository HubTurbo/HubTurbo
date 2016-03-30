package util;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public static void createDirectoryIfNonExistent(String fileDirectory) throws IOException {
        File directory = new File(fileDirectory);
        boolean isDirectoryExists = isDirectoryExists(fileDirectory);
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

    public static boolean isDirectoryExists(String directory) {
        File directoryFile = new File(directory);
        return directoryFile.exists() && directoryFile.isDirectory();
    }

    /**
     * Clears the content of a directory
     * @param directory directory whose contents are to be cleared
     */
    public static void clearDirectory(String directory) throws IOException {
        List<File> dirContent = listDirectoryContent(directory);

        for (File file : dirContent) {
            if (file.isDirectory()) {
                clearDirectory(file.getAbsolutePath());
            }

            if (!file.delete()) {
                throw new IOException("Failed to delete file " + file.getName() + "in directory" + directory);
            }
        }
    }

    public static List<File> listDirectoryContent(String directory) {
        File dirFile = new File(directory);

        return Arrays.asList(dirFile.listFiles());
    }

    public static boolean isFileExists(String fileDirectory, String fileName) {
        return Files.exists(getFilePath(fileDirectory, fileName));
    }

    public static void moveFile(Path source, Path dest, boolean isOverwrite) throws IOException {
        if (isOverwrite) {
            Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.move(source, dest);
        }
    }

    /**
     *
     * @param directorySource
     * @param directoryTarget
     * @return List of filenames failed to be moved
     */
    public static List<String> moveContentsOfADirectoryToAnother(String directorySource, String directoryTarget) {
        List<File> sourceFiles = FileHelper.listDirectoryContent(directorySource);
        List<String> failedToMoveFiles = new ArrayList<>();

        for (File sourceFile : sourceFiles) {
            Path sourceFilePath = sourceFile.toPath();
            Path targetFilePath = Paths.get(directoryTarget + File.separator + sourceFilePath.getFileName());

            try {
                FileHelper.moveFile(sourceFilePath, targetFilePath, true);
            } catch (IOException e) {
                logger.error("Failed to move file", e);
                failedToMoveFiles.add(sourceFile.getName());
            }
        }

        return failedToMoveFiles;
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
