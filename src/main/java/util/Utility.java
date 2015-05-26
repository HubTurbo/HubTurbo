package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.RepositoryId;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utility {

	private static final Logger logger = LogManager.getLogger(Utility.class.getName());

	/**
	 * Returns a CompletableFuture that will be completed 'later' with the given result.
	 * 'Later' is defined loosely. This implementation utilises a secondary thread to do it.
	 *
	 * The use case is if you want to return a CompletableFuture that just completes
	 * trivially, for example if you detect an error occurring early and don't want/need
	 * to go through the whole async task that the CompletableFuture represents. You can't
	 * complete the future synchronously because that wouldn't trigger all the callbacks
	 * attached to it.
	 *
	 * The name comes from the monadic interpretation of CompletableFutures.
	 * @param result the result that the unit CompletableFuture will be completed with
	 * @param <T> the type of the CompletableFuture result
	 * @return the unit future
	 */
	private static Executor unitFutureExecutor = Executors.newSingleThreadExecutor();
	public static <T> CompletableFuture<T> unitFutureOf(T result) {
		CompletableFuture<T> f = new CompletableFuture<>();
//		Platform.runLater(() -> f.complete(result));
		unitFutureExecutor.execute(() -> f.complete(result));
		return f;
	}

	public static boolean isWellFormedRepoId(String owner, String repo) {
		if (owner == null || owner.isEmpty() || repo == null || repo.isEmpty()) {
			return false;
		}
		return isWellFormedRepoId(RepositoryId.create(owner, repo).generateId());
	}

	public static boolean isWellFormedRepoId(String repoId) {
		return repoId != null && !repoId.isEmpty()
			&& RepositoryId.createFromId(repoId).generateId().equals(repoId);
	}

	public static Optional<String> readFile(String filename) {
		try {
			return Optional.of(new String(Files.readAllBytes(new File(filename).toPath())));
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return Optional.empty();
	}

	public static void writeFile(String fileName, String content) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			writer.println(content);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
		CompletableFuture<Void> allDoneFuture =
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
		return allDoneFuture.thenApply(v ->
				futures.stream().
					map(CompletableFuture::join)
					.collect(Collectors.<T>toList())
		);
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
        Pattern p = Pattern.compile("(^|_)([a-z])" );
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
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			return Optional.of(ge.getMaximumWindowBounds());
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return Optional.empty();
	}
}
