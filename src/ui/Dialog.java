package ui;

import java.util.concurrent.CompletableFuture;

public interface Dialog<T> {
	public CompletableFuture<T> show();
}
