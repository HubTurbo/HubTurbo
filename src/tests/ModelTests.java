package tests;

import static org.junit.Assert.*;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import model.Model;
import model.TurboIssue;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.junit.Test;

import tests.stubs.ModelStub;

@SuppressWarnings("unused")
public class ModelTests {

    private void ______MODEL_FUNCTIONALITY______() {
    }
    
    @Test
    public void loadingFromCache() {
        ModelStub model = new ModelStub();
        model.loadComponents(new RepositoryId("test", "testing"));
        
        assertEquals(model.getLabels().size(), 10);
        assertEquals(model.getMilestones().size(), 10);
        assertEquals(model.getCollaborators().size(), 10);
        assertEquals(model.getIssues().size(), 10);
    }

    @Test
    public void loadingFromGitHub() {
        ModelStub model = new ModelStub();
        model.loadComponents(new RepositoryId("test", "testing"));
        
        assertEquals(model.getLabels().size(), 10);
        assertEquals(model.getMilestones().size(), 10);
        assertEquals(model.getCollaborators().size(), 10);
        assertEquals(model.getIssues().size(), 10);
    }
    
    int numberOfUpdates = 0;

    private void ______ISSUES______() {
    }
        
    @Test
    public void loadIssuesTest() {
        ModelStub model = new ModelStub();
        assertEquals(model.getIssues().size(), 0);
        
        int start = numberOfUpdates;
        ListChangeListener<TurboIssue> listener = c -> ++numberOfUpdates;
        model.getIssuesRef().addListener(listener);
        model.loadIssues(TestUtils.getStubIssues(10));
        model.getIssuesRef().removeListener(listener);
        int end = numberOfUpdates;
        
        // All issues loaded
        assertEquals(model.getIssues().size(), 10);
        
        // Only one update triggered
        assertEquals(end - start, 1);
    }

    @Test
    public void getIndexOfIssueTest() {
        ModelStub model = new ModelStub();
        model.loadIssues(TestUtils.getStubIssues(10));

        for (int i=1; i<=10; i++) {
            assertEquals(model.getIndexOfIssue(i), i-1);
        }
    }

    @Test
    public void getIssueWithIdTest() {
        ModelStub model = new ModelStub();
        model.loadIssues(TestUtils.getStubIssues(10));

        for (int i=1; i<=10; i++) {
            assertNotEquals(model.getIssueWithId(i), null);
            assertTrue(model.getIssueWithId(i).getTitle().endsWith(i+""));
        }
    }
    
    private void ______CACHED_ISSUES______() {
    }
    
    @Test
    public void loadTurboIssuesTest() {
        ModelStub model = new ModelStub();
        assertEquals(model.getIssues().size(),  0);
        
        int start = numberOfUpdates;
        ListChangeListener<TurboIssue> listener = c -> ++numberOfUpdates;
        model.getIssuesRef().addListener(listener);
        model.loadTurboIssues(TestUtils.getStubTurboIssues(model, 10));
        model.getIssuesRef().removeListener(listener);
        int end = numberOfUpdates;
        
        // All issues loaded
        assertEquals(model.getIssues().size(), 10);
        
        // Only one update triggered
        assertEquals(end - start, 1);
    }
    
    @Test
    public void appendToCachedIssuesTest() {
        ModelStub model = new ModelStub();
        model.loadIssues(TestUtils.getStubIssues(10));
        TurboIssue issue11 = TestUtils.getStubTurboIssue(model, 11);
        model.appendToCachedIssues(issue11);
        assertTrue(model.getIssues().size() > 0);
        assertEquals(model.getIssueWithId(11), issue11);
        assertEquals(model.getIssues().get(0), issue11);
    }

    @Test
    public void updateCachedIssuesTest() {
        ModelStub model = new ModelStub();
        model.loadIssues(TestUtils.getStubIssues(10));
        
        Issue issue1 = TestUtils.getStubIssue(3);
        issue1.setTitle("something different");

        Issue issue2 = TestUtils.getStubIssue(11);
        issue2.setTitle("something really different");

        assertEquals(model.getIssueWithId(3).getTitle(), "issue3");
        assertEquals(model.getIssueWithId(11), null);

        model.updateCachedIssues(Arrays.asList(issue1, issue2), "testing/test");

        // 3 is there and has been changed
        // 11 is not there but is there after
        assertEquals(model.getIssueWithId(3).getTitle(), "something different");
        assertEquals(model.getIssueWithId(11).getTitle(), "something really different");
        assertEquals(model.getIssueWithId(11), model.getIssues().get(0));
    }

    @Test
    public void updateCachedIssueTest() {
    	
        ModelStub model = new ModelStub();
        model.loadIssues(TestUtils.getStubIssues(10));

        TurboIssue issue = TestUtils.getStubTurboIssue(model, 3);
        issue.setTitle("something different");
        // It's there
        assertEquals(model.getIssueWithId(3).getTitle(), "issue3");
        model.updateCachedIssue(issue);
        // It's been changed
        assertEquals(model.getIssueWithId(3).getTitle(), "something different");

        issue = TestUtils.getStubTurboIssue(model, 11);
        issue.setTitle("something really different");
        // It's not there
        assertEquals(model.getIssueWithId(11), null);
        model.updateCachedIssue(issue);
        // It's been added, and to the front
        assertEquals(model.getIssueWithId(11).getTitle(), "something really different");
        assertEquals(model.getIssueWithId(11), model.getIssues().get(0));
    }
    
    private void ______LABELS______() {
    }
    private void ______CACHED_LABELS______() {
    }
    private void ______MILESTONES______() {
    }
    private void ______CACHED_MILESTONES______() {
    }
    private void ______COLLABORATORS______() {
    }
    private void ______CACHED_COLLABORATORS______() {
    }
    private void ______RESOURCE_METADATA______() {
    }
        
}
