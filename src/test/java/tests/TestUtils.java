package tests;

import java.util.ArrayList;
import java.util.Arrays;

import backend.interfaces.IModel;
import backend.resource.*;
import prefs.Preferences;

public class TestUtils {

	public static final String REPO = "test/test";

	public static IModel singletonModel(Model model) {
		MultiModel models = new MultiModel(new Preferences(true));
		models.queuePendingRepository(model.getRepoId());
		models.addPending(model);
		models.setDefaultRepo(model.getRepoId());
		return models;
	}

	public static IModel modelWith(TurboIssue issue, TurboMilestone milestone) {
		return singletonModel(new Model(REPO,
			new ArrayList<>(Arrays.asList(issue)),
			new ArrayList<>(),
			new ArrayList<>(Arrays.asList(milestone)),
			new ArrayList<>()));
	}

	public static IModel modelWith(TurboIssue issue, TurboLabel label) {
		return singletonModel(new Model(new Model(REPO,
			new ArrayList<>(Arrays.asList(issue)),
			new ArrayList<>(Arrays.asList(label)),
			new ArrayList<>(),
			new ArrayList<>())));
	}

	public static IModel modelWith(TurboIssue issue, TurboUser user) {
		return singletonModel(new Model(new Model(REPO,
			new ArrayList<>(Arrays.asList(issue)),
			new ArrayList<>(),
			new ArrayList<>(),
			new ArrayList<>(Arrays.asList(user)))));
	}

	public static IModel modelWith(TurboIssue issue, TurboLabel label, TurboMilestone milestone) {
		return singletonModel(new Model(REPO,
			new ArrayList<>(Arrays.asList(issue)),
			new ArrayList<>(Arrays.asList(label)),
			new ArrayList<>(Arrays.asList(milestone)),
			new ArrayList<>()));
	}

	public static IModel modelWith(TurboIssue issue, TurboLabel label, TurboMilestone milestone, TurboUser user) {
		return singletonModel(new Model(REPO,
			new ArrayList<>(Arrays.asList(issue)),
			new ArrayList<>(Arrays.asList(label)),
			new ArrayList<>(Arrays.asList(milestone)),
			new ArrayList<>(Arrays.asList(user))));
	}

}
