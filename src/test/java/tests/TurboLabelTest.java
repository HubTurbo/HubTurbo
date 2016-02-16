package tests;

import backend.resource.TurboLabel;

import org.eclipse.egit.github.core.Label;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class TurboLabelTest {

    private static final String REPO = "test/test";

    @Test
    public void exclusive() {
        testWithDelimiter(TurboLabel.EXCLUSIVE_DELIMITER, true);
    }

    @Test
    public void nonexclusive() {
        testWithDelimiter(TurboLabel.NONEXCLUSIVE_DELIMITER, false);
    }

    public void testWithDelimiter(String delimiter, boolean shouldBeExclusive) {
        // group.name
        TurboLabel label = new TurboLabel(REPO, "group" + delimiter + "name");
        assertEquals(Optional.of("group"), label.getGroup());
        assertEquals("name", label.getName());
        assertEquals("group" + delimiter + "name", label.getActualName());
        assertEquals(shouldBeExclusive, label.isExclusive());

        // group.
        label = new TurboLabel(REPO, "group" + delimiter);
        assertEquals(Optional.of("group"), label.getGroup());
        assertEquals("", label.getName());
        assertEquals("group" + delimiter, label.getActualName());
        assertEquals(shouldBeExclusive, label.isExclusive());

        // The rest are unconditionally nonexlusive because there's no group.
        // The delimiter is taken to be part of the name instead.

        // name.
        label = new TurboLabel(REPO, delimiter + "name");
        assertEquals(Optional.<String>empty(), label.getGroup());
        assertEquals(delimiter + "name", label.getName());
        assertEquals(delimiter + "name", label.getActualName());
        assertEquals(false, label.isExclusive());

        // .
        label = new TurboLabel(REPO, delimiter);
        assertEquals(Optional.<String>empty(), label.getGroup());
        assertEquals(delimiter, label.getName());
        assertEquals(delimiter, label.getActualName());
        assertEquals(false, label.isExclusive());
    }

    @Test
    public void styleTest() {
        TurboLabel label1 = new TurboLabel(REPO, "name1");
        TurboLabel label2 = new TurboLabel(REPO, "name2");
        assertEquals(label1.getStyle(), label2.getStyle());
    }

    @Test
    public void turboLabelTest() {
        Label label = new Label();
        label.setName("test label");
        label.setColor("ffffff");
        TurboLabel turboLabel = new TurboLabel("dummy/dummy", label);
        assertEquals("dummy/dummy", turboLabel.getRepoId());

    }
    
    @Test
    public void matchedLabel() {
        List<TurboLabel> labels = new ArrayList<>();
        TurboLabel test = new TurboLabel(REPO, "test");
        labels.add(test);
        
        assertEquals(test, TurboLabel.getMatchingTurboLabel(labels, "test"));
    }
    
    @Test
    public void getLabelNames() {
        
        // test: confirms label names are unique
        TurboLabel test = new TurboLabel(REPO, "dummy");
        List<TurboLabel> labels = new ArrayList<>();
        labels.add(test);
        labels.add(test);

        assertEquals(1, TurboLabel.getLabelsNameList(labels).size());
    }

}
