package tests;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import backend.resource.Model;
import backend.resource.TurboIssue;
import ui.components.issue_creators.IssueCreatorPresenter;

public class IssueCreatorLogicTest {

    private Model repo;
    private TurboIssue issue;
    private IssueCreatorPresenter presenter;

    @Before
    public void init() {
        repo = mock(Model.class);
        issue = new TurboIssue("dummy/dummy", 1, "Old Issue");
        presenter = new IssueCreatorPresenter(repo, issue);
    }

    @Test
    public void isNewIssue() {
        assertFalse(presenter.isNewIssue());
    }
    
    @Test
    public void resolveTitle() {
        String expected = String.format("Editing Issue #%d", issue.getId());
        assertEquals(expected, presenter.resolveIssueTitle());
    }
    
}
