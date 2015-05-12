package backend;

import backend.interfaces.RepoCache;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.eclipse.egit.github.core.RepositoryId;

import java.io.*;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class JSONCache implements RepoCache {

	private final LinkedBlockingQueue<Task> tasks = new LinkedBlockingQueue<>();
	private final ExecutorService pool = Executors.newFixedThreadPool(1);

	public void init() {
		pool.execute(this::handleTask);
	}

	public boolean isRepoCached(String repoName) {
		File file = new File(escapeRepoName(repoName));
//		UI.instance.log(repoName + " => " + escapeRepoName(repoName));
		return file.exists() && file.isFile();
	}

	@Override
	public CompletableFuture<Model> loadRepository(String repoName) {
		CompletableFuture<Model> response = new CompletableFuture<>();
		tasks.add(new Task(repoName, response));
		return response;
	}

	@Override
	public void saveRepository(String repoId, SerializableModel model) {
		tasks.add(new Task(repoId, model));
	}

	public void handleTask() {
		try {
			Task task = tasks.take();

			if (task.load) {
				Model model = load(task.repoName);
				task.response.complete(model);
//				UI.instance.log("loaded issues from cache for " + task.repoName);
			} else {
				save(task.repoName, task.toSave);
//				UI.instance.log("saved issues to cache for " + task.repoName);
			}

			// Recurse
			pool.execute(this::handleTask);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void save(String repoName, SerializableModel model) {
		String output = new Gson().toJson(model);
//		UI.instance.log(output);
		String newRepoName = escapeRepoName(repoName);
		writeFile(newRepoName, output);
//		UI.instance.log("written to " + newRepoName);
	}

	private Model load(String repoName) {
		Optional<String> input = readFile(escapeRepoName(repoName));
		if (!input.isPresent()) {
			return new Model(RepositoryId.createFromId(repoName), UpdateSignature.empty);
		} else {
//			UI.instance.log("read from " + input);
			SerializableModel sModel = new Gson().fromJson(input.get(),
				new TypeToken<SerializableModel>(){}.getType());
//			UI.instance.log(input.get());
			return new Model(sModel);
		}
	}

	private static String escapeRepoName(String repoName) {
		return repoName.replace("/", "-") + ".json";
	}

	private void writeFile(String fileName, String content) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			writer.println(content);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private Optional<String> readFile(String filename) {
		try {
			return Optional.of(new String(Files.readAllBytes(new File(filename).toPath())));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	private class Task {
		public final boolean load;
		public final String repoName;
		public final CompletableFuture<Model> response;
		public final SerializableModel toSave;

		public Task(String repoName, SerializableModel toSave) {
			this.load = false;
			this.repoName = repoName;
			this.response = null;
			this.toSave = toSave;
		}

		public Task(String repoName, CompletableFuture<Model> response) {
			this.load = true;
			this.repoName = repoName;
			this.response = response;
			this.toSave = null;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Task task = (Task) o;

			if (repoName != null ? !repoName.equals(task.repoName) : task.repoName != null) return false;
			if (response != null ? !response.equals(task.response) : task.response != null) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = response != null ? response.hashCode() : 0;
			result = 31 * result + (repoName != null ? repoName.hashCode() : 0);
			return result;
		}
	}
}
