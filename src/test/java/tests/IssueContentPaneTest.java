package tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import ui.components.issue_creators.IssueContentPane;
import ui.components.issue_creators.IssueCreatorPresenter;

public class IssueContentPaneTest {

    private static IssueCreatorPresenter presenter;

    @BeforeClass 
    public static void init() {
        presenter = Mockito.mock(IssueCreatorPresenter.class);
    }


    // ==============
    // Helper methods
    // ==============
    
    private IssueContentPane createContentPane(String content) {
        return new IssueContentPane(content, presenter);
    }

    @Test
    public void test() {
        fail("Not yet implemented");
    }

}
