package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.IRepositoryIdProvider;

import java.util.function.Function;

/**
 * Nicer interface for using loggers
 */
public class HTLog {

	private static final Logger logger = HTLog.get(HTLog.class);

	public static Logger get(Class<?> c) {
		return LogManager.getLogger(c.getName());
	}

	public static String format(IRepositoryIdProvider repoId, String formatString, Object... args) {
		return repoId.generateId() + " | " + String.format(formatString, args);
	}

	public static String format(String repoId, String formatString, Object... args) {
		return repoId + " | " + String.format(formatString, args);
	}

	public static void error(Logger logger, String message) {
		logger.error(message);
	}

	public static void error(Logger logger, Exception e) {
		logger.error(e.getLocalizedMessage(), e);
	}

	/**
	 * Combinators for dealing with CompletableFutures
	 */

	/**
	 * For use as an argument to .exceptionally. Logs and returns null.
	 */
	public static <T> T log(Throwable e) {
		logger.error(e.getLocalizedMessage(), e);
		return null;
	}

	/**
	 * For use as an argument to .exceptionally. Logs and returns a given result.
	 */
	public static <T> Function<Throwable, T> withResult(T value) {
		return e -> {
			logger.error(e.getLocalizedMessage(), e);
			return value;
		};
	}
}
