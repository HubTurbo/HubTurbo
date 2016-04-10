package updater;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.DialogMessage;

import java.io.*;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Note: This class will be compiled into a JAR on its own
 *
 * Replaces a JAR file and optionally start that JAR
 * If you made any changes to this class, run gradle task compileJarUpdater
 *
 * Options:
 * --source string path of source JAR file to replace old JAR
 * --target string path of target JAR file to be replaced
 * --execute-jar "y" to execute target JAR after replacement
 * --backup-suffix (optional) suffix added to backup file
 */
public class JarUpdater extends Application {
    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    private static final int MAX_RETRY = 10;
    private static final int WAIT_TIME = 2000;
    private static final String BACKUP_FILENAME_SUFFIX = "_backup";
    private static final String ERROR_ON_UPDATING_MESSAGE =
            "Update cannot be applied. Update will be aborted. Please close this window.";

    private String backupSuffix = BACKUP_FILENAME_SUFFIX;


    public static void main(String[] args) { // NOPMD
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        initLogger();
        showWaitingWindow(stage);
        pool.execute(() -> {
            try {
                run();
            } catch (IllegalArgumentException e) {
                log(e.getMessage());
            } catch (IOException e) {
                log(e.getMessage());
                showErrorOnUpdatingDialog();
            }
        });
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

    private void run() throws IllegalArgumentException, IOException {
        HashMap<String, String> commandLineArgs = new HashMap<>(getParameters().getNamed());

        String sourceJarPath = commandLineArgs.get("source");
        String targetJarPath = commandLineArgs.get("target");
        String executeJarOption = commandLineArgs.get("execute-jar");
        String backupSuffixOption = commandLineArgs.get("backup-suffix");

        if (sourceJarPath == null || targetJarPath == null) {
            throw new IllegalArgumentException("Please specify source and target files.");
        } else {
            log("source: " + sourceJarPath);
            log("target: " + targetJarPath);
        }

        if (backupSuffixOption != null) {
            backupSuffix = backupSuffixOption;
        }

        File sourceJarFile = new File(sourceJarPath);
        File targetJarFile = new File(targetJarPath);

        if (!sourceJarFile.exists()) {
            throw new IOException(String.format("Source file %s does not exist. Aborting...",
                                                sourceJarFile.toString()));
        }

        if (targetJarFile.exists() && targetJarFile.isFile()) {
            makeJarBackup(targetJarFile);
        } else {
            JarUpdaterFileHandler.createParentDirectoriesOfFile(targetJarFile);
        }

        log("Moving source to target");
        try {
            JarUpdaterFileHandler.moveFileWithReplaceExisting(sourceJarFile.toPath(), targetJarFile.toPath());
        } catch (IOException e) {
            restoreBackupOfFile(targetJarFile);
            throw e;
        }

        // Optionally start target jar file
        if (executeJarOption != null && executeJarOption.equalsIgnoreCase("y") && !executeJar(targetJarPath)) {
            // if target can't be started, rollback backup
            restoreBackupOfFile(targetJarFile);
            executeJar(targetJarPath);
        }

        quit();
    }

    public void quit() {
        Platform.exit();
        System.exit(0);
    }

    /**
     * Makes back up of a JAR file.
     *
     * In some platforms (Windows in particular), JAR file cannot be modified if it was executed and
     * the process has not ended yet. As such, we will make several tries with wait in making backup.
     *
     * @param jarFile Jar file to be backed up
     * @throws IOException if making JAR backup fails
     */
    private void makeJarBackup(File jarFile) throws IOException {
        log("Making JAR backup");

        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                moveFileToBackup(jarFile);
                return;
            } catch (IOException e) {
                log(String.format("Failed to move file %s to backup. Might be due to original JAR still in use.",
                                  jarFile.getName()));
            }

            try {
                log("Wait for a while before trying again.");
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                log("Failed to wait for a while");
            }
        }

        throw new IOException("Jar file cannot be backed up. Most likely is in use by another process.");
    }

    /**
     * Moves file to its backup file (the original file will be removed)
     * @param file file to be moved to its backup
     * @throws IOException if failed to move file to backup
     */
    private void moveFileToBackup(File file) throws IOException {
        log("Moving file as backup");
        String backupFilename = getJarBackupFilename(file.getName());
        File backup = new File(file.getParent(), backupFilename);
        JarUpdaterFileHandler.moveFileWithReplaceExisting(file.toPath(), backup.toPath());
    }

    /**
     * Moves a backup file to its original file
     * @param file the original file to be recovered
     * @throws IOException if failed to move backup file
     */
    private void restoreBackupOfFile(File file) throws IOException {
        log("Restoring backup");
        String backupFilename = getJarBackupFilename(file.getName());
        File backup = new File(file.getParent(), backupFilename);
        JarUpdaterFileHandler.moveFileWithReplaceExisting(backup.toPath(), file.toPath());
    }

    /**
     * Gets backup name of jar file.
     * If backup suffix option is provided, it will be used instead of standard backup suffix.
     * If filename does not end with ".jar", appends standard backup suffix to the filename
     * @param filename filename which backup name should be generated
     * @return backup filename
     */
    private String getJarBackupFilename(String filename) {
        Pattern jarFilenamePattern = Pattern.compile("^(.*)\\.jar$", Pattern.CASE_INSENSITIVE);
        Matcher jarFilenameMatcher = jarFilenamePattern.matcher(filename);

        if (!jarFilenameMatcher.find()) {
            return filename + BACKUP_FILENAME_SUFFIX;
        }

        return jarFilenameMatcher.group(1) + backupSuffix + ".jar";
    }

    private static boolean executeJar(String jarPath) {
        return executeJar(jarPath, "");
    }

    /**
     * Runs a command to execute a JAR file with the given arguments and options
     * @param jarPath path (in string) to JAR file to be executed
     * @param argsAndOptions raw arguments and options to run the JAR
     * @return true if JAR can be executed, false otherwise
     */
    private static boolean executeJar(String jarPath, String argsAndOptions) {
        String command = String.format("java -jar %s %s", jarPath, argsAndOptions).trim();
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

    private void showErrorOnUpdatingDialog() {
        String header = "Failed to update";
        Platform.runLater(() -> {
            DialogMessage.showErrorDialog(header, ERROR_ON_UPDATING_MESSAGE);
            quit();
        });
    }

    /**
     * Not using Apache Logger since the library can't be included inside JAR
     * TODO include Apache Logger into jarUpdater
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

    public static void log(String message) {
        System.out.println(message);
    }
}
