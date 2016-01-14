package tests;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import util.Futures;

public class FuturesTest {

    @Test
    public void chainTest() {

        CompletableFuture<Integer> a = new CompletableFuture<>();
        CompletableFuture<Integer> b = new CompletableFuture<>();

        assertFalse(b.isDone());

        a.thenApply(Futures.chain(b));
        a.complete(1);

        assertTrue(b.isDone());
        assertEquals(1, (int) b.getNow(0));

    }
}
