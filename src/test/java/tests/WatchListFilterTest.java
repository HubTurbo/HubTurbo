package tests;

import org.junit.Test;
import ui.listpanel.ListPanel;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Tests non-UI parts of watch list feature as the UI parts are
 * already tested in ContextMenuTests
 */
public class WatchListFilterTest {

    @Test
    public void isWatchListFilter_validFilter_satisfiesRegex() {
        String validFilter1 = "";
        assertTrue(ListPanel.isWatchListFilter(validFilter1));

        String validFilter2 = "id:a/a#1";
        assertTrue(ListPanel.isWatchListFilter(validFilter2));

        String validFilter3 = "id:a/a#0;b/b#34";
        assertTrue(ListPanel.isWatchListFilter(validFilter3));
    }

    @Test
    public void isWatchListFilter_invalidFilter_doesNotSatisfyRegex() {
        String invalidFilter1 = "asdf";
        assertFalse(ListPanel.isWatchListFilter(invalidFilter1));

        String invalidFilter2 = "id:a/a#x";
        assertFalse(ListPanel.isWatchListFilter(invalidFilter2));

        String invalidFilter3 = "id:a/a#1 ; c";
        assertFalse(ListPanel.isWatchListFilter(invalidFilter3));

        String invalidFilter4 = "repo:xyz";
        assertFalse(ListPanel.isWatchListFilter(invalidFilter4));

        String invalidFilter5 = "id:ha";
        assertFalse(ListPanel.isWatchListFilter(invalidFilter5));
    }
}
