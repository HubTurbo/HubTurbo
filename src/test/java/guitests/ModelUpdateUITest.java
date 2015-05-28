package guitests;

import org.junit.Test;
import ui.RepositorySelector;
import ui.UI;
import util.events.UILogicRefreshEvent;
import util.events.UpdateDummyRepoEvent;

public class ModelUpdateUITest extends UITest {
    @Test
    public void modelUpdateUITest() throws InterruptedException {
        RepositorySelector repositorySelector = find("#repositorySelector");
        click(repositorySelector);
        for (int i = 0; i < 10; i++) {
            click("View");
            click("Refresh");
            sleep(500);
            UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.NEW_ISSUE));
            UI.events.triggerEvent(new UILogicRefreshEvent());
            sleep(500);
        }
        sleep(1000);
    }
}
