package util;

import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Combinators for dealing with CompletableFutures.
 */
public final class Futures {

    private static final Logger logger = HTLog.get(Futures.class);

    private Futures() {
    }

    /**
     * Returns a CompletableFuture that will be completed 'later' with the given result.
     * 'Later' is defined loosely. This implementation utilises a secondary thread to do it.
     * <p>
     * The use case is if you want to return a CompletableFuture that just completes
     * trivially, for example if you detect an error occurring early and don't want/need
     * to go through the whole async task that the CompletableFuture represents. You can't
     * complete the future synchronously because that wouldn't trigger all the callbacks
     * attached to it.
     * <p>
     * The name comes from the monadic interpretation of CompletableFutures.
     *
     * @param result the result that the unit CompletableFuture will be completed with
     * @param <T> the type of the CompletableFuture result
     * @return the unit future
     */
    private static Executor unitFutureExecutor = Executors.newSingleThreadExecutor();

    public static <T> CompletableFuture<T> unit(T result) {
        CompletableFuture<T> f = new CompletableFuture<>();
        unitFutureExecutor.execute(() -> f.complete(result));
        return f;
    }

    /**
     * For use as an argument to thenApply. Given
     * <p>
     * a.thenApply(chain(b));
     * <p>
     * If a completes with some value v, then b will also complete with v.
     */
    public static <T> Function<T, T> chain(CompletableFuture<T> other) {
        return a -> {
            other.complete(a);
            return a;
        };
    }

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

    /**
     * Turns a List<Future<T>> into a Future<List<T>>. In other words, ensures that all
     * futures in a collection complete and gathers their results.
     */
    public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v -> futures.stream()
                                                   .map(CompletableFuture::join)
                                                   .collect(Collectors.<T>toList()));
    }
}
