package tests;

import backend.interfaces.IModel;
import backend.resource.*;
import org.apache.commons.io.IOUtils;
import org.mockserver.model.Header;
import ui.TestController;
import ui.UI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TestUtils {

    public static final String REPO = "test/test";

    private TestUtils() {}

    public static IModel singletonModel(Model model) {
        MultiModel models = new MultiModel(TestController.createTestPreferences());
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

    /**
     * Wrapper for Thread.sleep. Taken from TickingTimerTests.
     *
     * @param seconds The number of seconds for the thread to sleep.
     */
    public static void delay(double seconds) {
        UI.status.updateTimeToRefresh((int) seconds);
        int time = (int) (seconds * 1000);
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String readFileFromResource(Object callingObject, String filename) throws IOException {
        ClassLoader classLoader = callingObject.getClass().getClassLoader();
        return IOUtils.toString(classLoader.getResourceAsStream(filename));
    }

    /**
     * Parses a string containing HTTP header fields and return a List of Header to be used with MockServer
     * @param header
     * @return
     */
    public static List<Header> parseHeaderRecord(String header) {
        List<Header> result = new ArrayList<>();
        String[] fields = header.split("[\r\n]+");

        for (String field : fields) {
            String[] nameAndValue = field.split(":", 2);
            if (nameAndValue.length == 2) {
                result.add(new Header(nameAndValue[0].trim(), nameAndValue[1].trim()));
            }
        }

        return result;
    }
}
