package tests;

import backend.RepoIO;
import backend.control.RepoOpControl;
import backend.interfaces.IModel;
import backend.resource.*;
import org.apache.commons.io.IOUtils;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import ui.TestController;
import ui.UI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestUtils {

    public static final String REPO = "test/test";
    public static final String API_PREFIX = "/api/v3";

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

    /**
     * Creates a GitHub API v3 request that can be used with MockServer and GitHubClient
     * @param method the HTTP request method
     * @param page the page of the resource being requested
     * @param repoStringId string version of the repo id
     * @param repoNumericId numeric version of the repo id returned in the Link header
     * @param apiSegments segments of the request path after the repo id
     * @return
     */
    public static HttpRequest createMockServerRequest(String method, int page,
                                                      String repoStringId, String repoNumericId,
                                                      String apiSegments) {
        String repoSegment = page == 1 ? "/repos/" + repoStringId : "/repositories/" + repoNumericId;
        String path = API_PREFIX + repoSegment + apiSegments;

        return HttpRequest.request()
                .withMethod(method)
                .withPath(path)
                .withQueryStringParameter("state", "all")
                .withQueryStringParameter("per_page", "100")
                .withQueryStringParameter("page", Integer.toString(page));
    }

    /**
     * Delays for a specified time then calls a function and returns its result
     * @param delay time to be delayed in milliseconds
     * @param supplier
     * @return
     * @throws InterruptedException
     */
    public static <T> T delayThenGet(long delay, Supplier<T> supplier) throws InterruptedException {
        Thread.sleep(delay);
        return supplier.get();
    }

    /**
     * Delays for a specified time then runs a function
     * @param delay time to be delayed in milliseconds
     * @param runnable
     * @return
     * @throws InterruptedException
     */
    public static void delayThenRun(long delay, Runnable runnable) throws InterruptedException {
        Thread.sleep(delay);
        runnable.run();
    }

    /**
     * Creates a RepoOpControl singleton instance that RepoIO requires for some operations but often
     * not yet created e.g. when RepoIO is tested alone without any Logic instance. The RepoOpControl
     * also includes an empty MultiModel.
     * @param repoIO
     */
    public static RepoOpControl createTestRepoOpControl(RepoIO repoIO) {
        MultiModel models = mock(MultiModel.class);
        when(models.getModelById(anyString())).thenReturn(Optional.empty());
        return RepoOpControl.createRepoOpControl(repoIO, models);
    }

}
