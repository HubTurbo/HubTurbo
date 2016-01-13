package undo.actions;

import java.util.concurrent.CompletableFuture;

public interface Action<T> {

    String getDescription();

    CompletableFuture<Boolean> act(T t);

    CompletableFuture<Boolean> undo(T t);

}
