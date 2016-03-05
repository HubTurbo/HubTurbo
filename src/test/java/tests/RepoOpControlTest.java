package tests;

import backend.RepoIO;
import backend.control.RepoOpControl;
import backend.resource.Model;
import backend.resource.MultiModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import util.AtomicMaxInteger;
import util.Futures;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RepoOpControlTest {

    private static final Logger logger = LogManager.getLogger(RepoOpControlTest.class.getName());

    private static final String REPO = "test/test";

    private final Executor executor = Executors.newCachedThreadPool();

    // TODO: replace this test
    /*
    @Test
    public void opsWithinMultipleRepos() throws ExecutionException, InterruptedException {

        // Operations on different repositories can execute concurrently

        AtomicMaxInteger counter = new AtomicMaxInteger(0);
        RepoOpControl control = new RepoOpControl(stubbedRepoIO(counter));

        List<CompletableFuture<Model>> futures = new ArrayList<>();

        futures.add(control.openRepository(REPO));
        futures.add(control.updateModel(new Model(REPO + 1)));
        futures.add(control.openRepository(REPO));
        futures.add(control.updateModel(new Model(REPO + 1)));
        control.removeRepository(REPO + 2).get();
        control.removeRepository(REPO + 2).get();

        Futures.sequence(futures).get();

        assertEquals(3, counter.getMax());
    }

    @Test
    public void opsWithinSameRepo() throws ExecutionException, InterruptedException {

        // Operations on the same repository cannot execute concurrently

        AtomicMaxInteger counter = new AtomicMaxInteger(0);
        RepoOpControl control = new RepoOpControl(stubbedRepoIO(counter), MultiModel);

        List<CompletableFuture<Model>> futures = new ArrayList<>();

        futures.add(control.openRepository(REPO));
        control.removeRepository(REPO).get();
        futures.add(control.updateModel(new Model(REPO)));
        control.removeRepository(REPO).get();
        futures.add(control.openRepository(REPO));
        futures.add(control.updateModel(new Model(REPO)));

        Futures.sequence(futures).get();

        assertEquals(1, counter.getMax());
    }*/

    @Test
    public void openingSameRepo() throws ExecutionException, InterruptedException {

        // We cannot open the same repository concurrently

        AtomicMaxInteger counter = new AtomicMaxInteger(0);
        RepoOpControl control = RepoOpControl.createRepoOpControl(stubbedRepoIO(counter), mock(MultiModel.class));

        List<CompletableFuture<Model>> futures = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            futures.add(control.openRepository(REPO));
        }
        Futures.sequence(futures).get();

        assertEquals(1, counter.getMax());
    }

    @Test
    public void openingDifferentRepo() throws ExecutionException, InterruptedException {

        // We can open different repositories concurrently

        AtomicMaxInteger counter = new AtomicMaxInteger(0);
        RepoOpControl control = RepoOpControl.createRepoOpControl(stubbedRepoIO(counter), mock(MultiModel.class));

        List<CompletableFuture<Model>> futures = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            futures.add(control.openRepository(REPO + i));
        }
        Futures.sequence(futures).get();

        assertEquals(3, counter.getMax());
    }

    /**
     * Creates a stub RepoIO with artificial delay for various operations, and
     * which increments a value for purposes of verifying behaviour.
     */
    private RepoIO stubbedRepoIO(AtomicMaxInteger counter) {

        RepoIO stub = mock(RepoIO.class);

        when(stub.openRepository(REPO))
            .then(invocation -> createResult(counter, new Model(REPO)));
        when(stub.removeRepository(REPO))
            .then(invocation -> createResult(counter, true));
        when(stub.updateModel(new Model(REPO)))
            .then(invocation -> createResult(counter, new Model(REPO)));

        for (int i = 0; i < 3; i++) {
            when(stub.openRepository(REPO + i))
                .then(invocation -> createResult(counter, new Model(REPO)));
            when(stub.removeRepository(REPO + i))
                .then(invocation -> createResult(counter, true));
            when(stub.updateModel(new Model(REPO + i)))
                .then(invocation -> createResult(counter, new Model(REPO)));
        }

        return stub;
    }

    /**
     * Creates a result value which completes after a short delay, to simulate an async task.
     */
    private <T> CompletableFuture<T> createResult(AtomicMaxInteger counter, T value) {

        CompletableFuture<T> result = new CompletableFuture<>();

        executor.execute(() -> {
            counter.increment();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            counter.decrement();
            result.complete(value);
        });

        return result;
    }
}
