package util;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.RepositoryId;

import ui.UI;
import util.events.ShowErrorDialogEvent;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public class Utility {

    private static final Logger logger = LogManager.getLogger(Utility.class.getName());

    public static boolean isWellFormedRepoId(String owner, String repo) {
        return !(owner == null || owner.isEmpty() || repo == null || repo.isEmpty())
                && isWellFormedRepoId(RepositoryId.create(owner, repo).generateId());
    }

    public static boolean isWellFormedRepoId(String repoId) {
        RepositoryId repositoryId = RepositoryId.createFromId(repoId);
        return repoId != null && !repoId.isEmpty() && repositoryId != null
            && repositoryId.generateId().equals(repoId);
    }

    public static Optional<String> readFile(String filename) {
        try {
            return Optional.of(new String(Files.readAllBytes(new File(filename).toPath())));
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Optional.empty();
    }

    public static boolean writeFile(String fileName, String content, int issueCount) {
        try {
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            writer.println(content);
            writer.close();

            long sizeAfterWrite = Files.size(Paths.get(fileName));
            return processFileGrowth(sizeAfterWrite, issueCount, fileName);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
            return true;
        }
    }

    private static boolean processFileGrowth(long sizeAfterWrite, int issueCount, String fileName) {
        // The average issue is about 0.75KB in size. If the total filesize is more than (2 * issueCount KB),
        // we consider the json to have exploded as the file is unusually large.
        if (sizeAfterWrite > ((long) issueCount * 2000)) {
            UI.events.triggerEvent(new ShowErrorDialogEvent("Possible data corruption detected",
                    fileName + " is unusually large.\n\n"
                            + "Now proceeding to delete the file and redownload the repository to prevent "
                            + "further corruption.\n\n"
                            + "A copy of the corrupted file is saved as " + fileName + "-err. "
                            + "The error log of the program has been stored in the file hubturbo-err-log.log."
                    )
            );
            parseAndDeleteFile(fileName);
            copyLog();
            return true;
        }
        return false;
    }

    public static void copyLog() {
        try {
            Files.copy(Paths.get("hubturbo-log.log"),
                    Paths.get("hubturbo-err-log.log"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public static void parseAndDeleteFile(String fileName) {
        try {
            Path corruptedFile = Paths.get(fileName);
            if (Files.exists(corruptedFile)) {
                String corruptedFileData = readFile(fileName).get();
                PrintWriter writer = new PrintWriter(fileName + "-err", "UTF-8");
                writer.println(new GsonBuilder().setPrettyPrinting().create().toJson(
                        new JsonParser().parse(corruptedFileData)
                ));
                writer.close();

                Files.delete(corruptedFile);
            }
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public static String stripQuotes(String s) {
        return s.replaceAll("^\"|\"$", "");
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    public static Date parseHTTPLastModifiedDate(String dateString) {
        assert dateString != null;
        try {
            return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").parse(dateString);
        } catch (ParseException e) {
            assert false : "Error in date format string!";
        }
        // Should not happen
        return null;
    }

    public static String formatDateISO8601(Date date){
        assert date != null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(date);
    }

    public static Date localDateTimeToDate(LocalDateTime time) {
        assert time != null;
        return new Date(localDateTimeToLong(time));
    }

    public static LocalDateTime dateToLocalDateTime(Date date) {
        assert date != null;
        return longToLocalDateTime(date.getTime());
    }

    public static long localDateTimeToLong(LocalDateTime t) {
        assert t != null;
        return t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static LocalDateTime longToLocalDateTime(long epochMilli) {
        Instant instant = new Date(epochMilli).toInstant();
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public static long millisecToMinutes(long millisecDuration) {
        return millisecDuration / 1000 / 60;
    }

    public static long minutesFromNow(long targetTime) {
        return millisecToMinutes(targetTime - new Date().getTime());
    }

    /**
     * Parses a version number string in the format V1.2.3.
     * @param version version number string
     * @return an array of 3 elements, representing the major, minor, and patch versions respectively
     */
    public static Optional<int[]> parseVersionNumber(String version) {
        // Strip non-digits
        version = version.replaceAll("[^0-9.]+", "");

        String[] temp = version.split("\\.");
        try {
            int major = temp.length > 0 ? Integer.parseInt(temp[0]) : 0;
            int minor = temp.length > 1 ? Integer.parseInt(temp[1]) : 0;
            int patch = temp.length > 2 ? Integer.parseInt(temp[2]) : 0;
            return Optional.of(new int[] {major, minor, patch});
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static String version(int major, int minor, int patch) {
        return String.format("V%d.%d.%d", major, minor, patch);
    }

    public static String snakeCaseToCamelCase(String str) {
        Pattern p = Pattern.compile("(^|_)([a-z])");
        Matcher m = p.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, m.group(2).toUpperCase());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static Rectangle getScreenDimensions() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Rectangle((int) screenSize.getWidth(), (int) screenSize.getHeight());
    }

    public static Optional<Rectangle> getUsableScreenDimensions() {
        try {
            if (PlatformSpecific.isOnLinux()) {
                UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
            } else {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            }

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            return Optional.of(ge.getMaximumWindowBounds());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Optional.empty();
    }
}
