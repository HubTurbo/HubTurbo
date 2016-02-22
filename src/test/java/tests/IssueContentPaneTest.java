package tests;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import ui.components.issuecreator.IssueContentPane;
import ui.components.issuecreator.IssueCreatorPresenter;

public class IssueContentPaneTest {

    private static IssueCreatorPresenter presenter;

    @BeforeClass 
    public static void init() {
        presenter = Mockito.mock(IssueCreatorPresenter.class);
    }


    @Test
    public void getAllUsers() {
    }

    private IssueContentPane createContentPane(String content) {
        return new IssueContentPane(content, presenter);
    }


}
