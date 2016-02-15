package updater;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Note: This class will be compiled into a JAR on its own
 *
 * Replaces a JAR file and optionally start that JAR
 *
 * Options:
 * --source string path of source JAR file to replace old JAR
 * --target string path of target JAR file to be replaced
 * --execute-jar "y" to execute target JAR after replacement
 */
public class JarUpdater extends Application {
    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    private static final int MAX_RETRY = 10;
    private static final int WAIT_TIME = 2000;
    private static final String BACKUP_FILENAME_SUFFIX = "_backup";

    @SuppressWarnings("PMD")
    // PMD - "Consider using varargs for methods or constructors which take an array the last parameter."
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        initLogger();
        showWaitingWindow(stage);
        pool.execute(() -> run());
    }

    private void showWaitingWindow(Stage stage) {
        stage.setTitle("Applying Updates");
        VBox windowMainLayout = new VBox();
        Group root = new Group();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.setRoot(windowMainLayout);

        Label updatingLabel = new Label();
        updatingLabel.setText("Please wait. HubTurbo is being updated...");
        updatingLabel.setPadding(new Insets(50));

        windowMainLayout.getChildren().addAll(updatingLabel);

        stage.show();
    }

    private void run() {
        HashMap<String, String> commandLineArgs = new HashMap<>(getParameters().getNamed());

        String sourceJarPath = commandLineArgs.get("source");
        String targetJarPath = commandLineArgs.get("target");
        String executeJarOption = commandLineArgs.get("execute-jar");

        if (sourceJarPath == null || targetJarPath == null) {
            log("Please specify source and target files.");
            quit();
        } else {
            log("source: " + sourceJarPath);
            log("target: " + targetJarPath);
        }

        File sourceJarFile = new File(sourceJarPath);
        File targetJarFile = new File(targetJarPath);

        if (!sourceJarFile.exists()) {
            log("Such source file does not exist, aborting.");
            quit();
        }

        prepareTargetJarFile(targetJarFile);

        log("Moving source to target");
        if (!moveFile(sourceJarFile.toPath(), targetJarFile.toPath())) {
            log("Failed to move update.");
            restoreBackup(targetJarFile);
            quit();
        }

        // Optionally start target jar file
        if (executeJarOption != null && executeJarOption.equalsIgnoreCase("y")) {
            if (executeJar(targetJarPath, "")) {
                removeBackup(targetJarFile);
            } else { // if target can't be started, rollback backup
                restoreBackup(targetJarFile);
                executeJar(targetJarPath, "");
            }
        } else {
            removeBackup(targetJarFile);
        }

        quit();
    }

    private boolean moveFile(Path source, Path dest) {
        log("Moving file from:" + source.toString() + " to:" + dest.toString());
        try {
            if (!dest.toFile().exists() && !dest.toFile().createNewFile()) {
                return false;
            }
            Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log("Failed to move file");
            log(e.toString());
            return false;
        }

        return true;
    }

    public void quit() {
        Platform.exit();
        System.exit(0);
    }

    /**
     * Creates directories for target file if they do not exist.
     * Otherwise, makes backup if target file exists.
     */
    private void prepareTargetJarFile(File targetJarFile) {
        if (targetJarFile.getParentFile() != null &&
                !createDirectories(targetJarFile.getParentFile().getAbsoluteFile())) {
            log("Failed to make directories of target file.");
            quit();
        } else if (targetJarFile.exists() && !makeJarBackup(targetJarFile)) {
            log("Failed to make backup.");
            quit();
        }
    }

    /**
     * Makes back up of a JAR file.
     *
     * In some platforms (Windows in particular), JAR file cannot be modified if it was executeded and
     * the process has not ended yet. As such, we will make several tries with wait in making backup.
     *
     * @param jarFile Jar file to be backed up
     * @return true if backup is created, false otherwise
     */
    private boolean makeJarBackup(File jarFile) {
        log("Making JAR backup");

        for (int i = 0; i < MAX_RETRY; i++) {
            if (moveFileAsBackup(jarFile)) {
                return true;
            }

            log("Failed to make backup. Might be due to original JAR still in use.");
            try {
                log("Wait for a while before trying again.");
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                log("Failed to wait for a while");
            }
        }

        return false;
    }

    private boolean moveFileAsBackup(File file) {
        log("Moving file as backup");
        String backupFilename = getBackupFilename(file.getName());
        File backup = new File(file.getParent(), backupFilename);
        return moveFile(file.toPath(), backup.toPath());
    }

    private boolean restoreBackup(File file) {
        log("Restoring backup");
        String backupFilename = getBackupFilename(file.getName());
        File backup = new File(file.getParent(), backupFilename);
        return moveFile(backup.toPath(), file.toPath());
    }

    private void removeBackup(File file) {
        log("Removing backup");
        String backupFilename = getBackupFilename(file.getName());
        File backup = new File(file.getParent(), backupFilename);
        if (backup.exists() && !backup.delete()) {
            log("Failed to remove backup");
        }
    }

    private String getBackupFilename(String filename) {
        return filename + BACKUP_FILENAME_SUFFIX;
    }

    private boolean createDirectories(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            log("Failed to create directories for file " + dir.getName());
            return false;
        }

        return true;
    }

    /**
     * Runs a command to execute a JAR file with the given arguments and options
     * @param jarPath path (in string) to JAR file to be executed
     * @param argsAndOptions raw arguments and options to run the JAR
     * @return true if JAR can be executed, false otherwise
     */
    private static boolean executeJar(String jarPath, String argsAndOptions) {
        String command = String.format("java -jar %1$s %2$s", jarPath, argsAndOptions).trim();
        log("Starting JAR with command: " + command);

        Process process;

        try {
            process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            log("Failed to run command. Error: " + e.toString());
            return false;
        }

        return process.isAlive();
    }

    /**
     * Not using Apache Logger since it will require another initialization
     */
    private void initLogger() {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(new File("update.log")),
                    true, "UTF-8");
            System.setOut(out);
        } catch (FileNotFoundException e) {
            log("File not found, will not create logger");
        } catch (UnsupportedEncodingException e) {
            log("Encoding not supported, will not create logger");
        }
    }

    private static void log(String message) {
        System.out.println(message);
    }
}
