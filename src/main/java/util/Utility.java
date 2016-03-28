package util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.RepositoryId;
import ui.UI;
import util.events.ShowErrorDialogEvent;

import java.io.*;
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
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Utility {

    private static final Logger logger = LogManager.getLogger(Utility.class.getName());

    public static boolean isWellFormedRepoId(String owner, String repo) {
        return !(owner == null || owner.isEmpty() || repo == null || repo.isEmpty())
                && isWellFormedRepoId(RepositoryId.create(owner, repo).generateId());
    }

    public static boolean isWellFormedRepoId(String repoId) {
        RepositoryId repositoryId = RepositoryId.createFromId(repoId);
        return repoId != null && !repoId.isEmpty() && repositoryId != null
                && repoId.equals(repositoryId.generateId());
    }

    public static Optional<String> readFile(String fileName) {
        boolean validPath = !(fileName == null || fileName.isEmpty());
        if (validPath) {
            try {
                return Optional.of(new String(Files.readAllBytes(new File(fileName).toPath()), "UTF-8"));
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns true on JSON corruption.
     * TODO remove JSON-specific parts
     *
     * @param fileName
     * @param content
     * @param issueCount
     * @return
     */
    public static boolean writeFile(String fileName, String content, int issueCount) {
        boolean validPath = !(fileName == null || fileName.isEmpty());
        if (validPath) {
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(fileName), "UTF-8"
                ));
                writer.write(content);
                writer.newLine();
                writer.close();

                long sizeAfterWrite = Files.size(Paths.get(fileName));
                return processFileGrowth(sizeAfterWrite, issueCount, fileName);
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage(), e);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true on failure to delete file
     *
     * @param fileName
     * @return true on error in deleting file, false otherwise
     */
    public static boolean deleteFile(String fileName) {
        boolean validPath = !(fileName == null || fileName.isEmpty());
        if (validPath) {
            try {
                Path repoFile = Paths.get(fileName);
                Files.delete(repoFile);
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage(), e);
                return true;
            }
        }
        return false;
    }

    private static boolean processFileGrowth(long sizeAfterWrite, int issueCount, String fileName) {
        // The average issue is about 0.75KB in size. If the total filesize is more than (2 * issueCount KB),
        // we consider the json to have exploded as the file is unusually large.
        if (issueCount > 0 && sizeAfterWrite > ((long) issueCount * 2000)) {
            UI.events.triggerEvent(new ShowErrorDialogEvent("Possible data corruption detected",
                    fileName + " is unusually large.\n\n"
                            + "Now proceeding to delete the file and "
                            + "redownload the repository to prevent "
                            + "further corruption.\n\n"
                            + "A copy of the corrupted file is saved as "
                            + fileName + "-err. "
                            + "The error log of the program has been stored "
                            + "in the file hubturbo-err-log.log."
            ));
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
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(fileName + "-err"), "UTF-8"
                ));
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(
                        new JsonParser().parse(corruptedFileData)
                ));
                writer.newLine();
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

    public static String removeAllWhitespace(String s) {
        return s.replaceAll("\\s", "");
    }

    public static String join(List<String> list, String delimiter) {
        return list.stream().collect(Collectors.joining(delimiter));
    }

    /**
     * Returns a replacement object if obj is null
     *
     * @param obj
     * @param replacement
     * @return a replacement value if obj is null
     */
    public static <T> T replaceNull(T obj, T replacement) {
        return obj == null ? replacement : obj;
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    public static Date parseHTTPLastModifiedDate(String dateString) {
        assert dateString != null;
        try {
            return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).parse(dateString);
        } catch (ParseException e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new IllegalArgumentException("Could not parse date " + dateString, e);
        }
    }

    public static String formatDateISO8601(Date date) {
        assert date != null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US);
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
     *
     * @param version version number string
     * @return an array of 3 elements, representing the major, minor, and patch versions respectively
     */
    public static Optional<int[]> parseVersionNumber(String version) {
        // Strip non-digits
        String numericVersion = version.replaceAll("[^0-9.]+", "");

        String[] temp = numericVersion.split("\\.");
        try {
            int major = temp.length > 0 ? Integer.parseInt(temp[0]) : 0;
            int minor = temp.length > 1 ? Integer.parseInt(temp[1]) : 0;
            int patch = temp.length > 2 ? Integer.parseInt(temp[2]) : 0;
            return Optional.of(new int[] { major, minor, patch });
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

    public static boolean containsIgnoreCase(String source, String query) {
        return source.toLowerCase().contains(query.toLowerCase());
    }

    /**
     * Checks that the source contains all words in queries
     *
     * @param source
     * @param queries
     * @return
     */
    public static boolean containsIgnoreCase(String source, List<String> queries) {
        return queries.stream().allMatch(query -> Utility.containsIgnoreCase(source, query));
    }

    public static boolean startsWithIgnoreCase(String source, String query) {
        return source.toLowerCase().startsWith(query.toLowerCase());
    }

    public static String getNameClosestToDesiredName(String desiredName, List<String> existingNames) {
        String availableName = desiredName;

        if (!existingNames.contains(desiredName)) {
            return availableName;
        }

        List<String> existingSuffixes = existingNames.stream()
                .filter(existing -> existing.startsWith(desiredName)
                        && !existing.equalsIgnoreCase(desiredName))
                .map(existing -> existing.substring(existing.indexOf(desiredName, 0) + desiredName.length()))
                .collect(Collectors.toList());

        int index = 1;

        while (existingSuffixes.contains(Integer.toString(index))) {
            index++;
        }

        availableName = desiredName + Integer.toString(index);

        return availableName;
    }

    // TODO: remove once #1078 is solved from all repoIds normalization
    public static Set<String> convertSetToLowerCase(Set<String> originalSet) {
        return originalSet.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }

    /**
     * If a value is present in the optional, applies mapping function to it and return the result,
     * otherwise executes the ifEmpty function and returns an empty optional
     *
     * @param optional
     * @param mapper
     * @param ifEmpty
     * @return
     */
    public static <T, U> Optional<U> safeFlatMapOptional(Optional<T> optional,
                                                         Function<? super T, Optional<U>> mapper,
                                                         Runnable ifEmpty) {
        if (optional.isPresent()) {
            return mapper.apply(optional.get());
        }
        ifEmpty.run();
        return Optional.empty();
    }

    private Utility() {}

}
