package updater;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Utility class to do file handling for Jar Updater
 * // TODO merge with standard Utility.class once Apache Logger can be bundled into JarUpdater Jar file
 */
public final class JarUpdaterFileHandler {

    /**
     * Creates parent directories of a given file
     * @param file file which parent directories would like to be created
     * @throws IOException if any parent directories cannot be created
     */
    public static void createParentDirectoriesOfFile(File file) throws IOException {
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.mkdirs()) {
            throw new IOException(String.format("Failed to create parent directories of %s.", file.getAbsolutePath()));
        }
    }

    /**
     * Moves a file from source to destination. Replaces destination file if it exists.
     * @param source source file to move from
     * @param dest destination file to move to
     * @throws IOException if failed to create destination file or failed to move file
     */
    public static void moveFileWithReplaceExisting(Path source, Path dest) throws IOException {
        JarUpdater.log("Moving file from:" + source.toString() + " to:" + dest.toString());

        Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
    }

    private JarUpdaterFileHandler() {}
}
