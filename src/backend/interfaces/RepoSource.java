package backend.interfaces;

import backend.Model;
import backend.UserCredentials;

import java.util.concurrent.CompletableFuture;

public interface RepoSource {
	public CompletableFuture<Boolean> login(UserCredentials credentials);
	public CompletableFuture<Model> downloadRepository(String repoId);
}
