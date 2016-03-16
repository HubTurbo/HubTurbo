package tests;

import backend.resource.TurboLabel;

import org.eclipse.egit.github.core.Label;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class TurboLabelTest {

    private static final String REPO = "test/test";

    @Test
    public void isExclusive_exclusiveLabels_true() {
        testWithDelimiter(TurboLabel.EXCLUSIVE_DELIMITER, true);
    }

    @Test
    public void isExclusive_nonExclusiveLabels_false() {
        testWithDelimiter(TurboLabel.NONEXCLUSIVE_DELIMITER, false);
    }

    @Test
    public void consistentStyle() {
        // Ensures that default labels share the same style
        TurboLabel label1 = new TurboLabel(REPO, "name1");
        TurboLabel label2 = new TurboLabel(REPO, "name2");
        assertEquals(label1.getStyle(), label2.getStyle());
    }

    @Test
    public void labelRepoId() {
        Label label = new Label();
        label.setName("test label");
        label.setColor("ffffff");
        TurboLabel turboLabel = new TurboLabel("dummy/dummy", label);
        assertEquals("dummy/dummy", turboLabel.getRepoId());

    }
    
    @Test
    public void getMatchingTurboLabel_multipleMatches_firstMatch() {
        List<TurboLabel> labels = new ArrayList<>();
        labels.add(new TurboLabel(REPO, "test"));
        labels.add(new TurboLabel(REPO, "testing"));
        
        // Ensures return of first matching label
        assertEquals("test", TurboLabel.getFirstMatchingTurboLabel(labels, "test").getFullName());
    }
    
    @Test
    public void getLabelsNameList_labelsWithGroup_labelsFullName() {
        TurboLabel test = new TurboLabel(REPO, "test.a");
        TurboLabel test2 = new TurboLabel(REPO, "dummy-a");
        List<TurboLabel> labels = new ArrayList<>();
        labels.add(test);
        labels.add(test2);

        // Ensures result matches each label actual name even with delimiter
        assertEquals(Arrays.asList("test.a", "dummy-a"), TurboLabel.getLabelNames(labels));
    }

    @Test
    public void splitKeyword_consecutiveDelimiters_notInGroup() {
        TurboLabel test = new TurboLabel(REPO, "..");
        assertFalse(test.isInGroup());
    }


    @Test
    public void getMatchedLabels_withLabelNames() {
        List<String> labelNames = Arrays.asList("homework", "lab");
        List<TurboLabel> labels = labelNames.stream()
            .map(name -> new TurboLabel("", name)).collect(Collectors.toList());
        
        assertEquals(labels, TurboLabel.getMatchedLabels(labels, labelNames));
    }

    private void testWithDelimiter(String delimiter, boolean shouldBeExclusive) {
        // label format: group.name
        TurboLabel label = new TurboLabel(REPO, "group" + delimiter + "name");
        assertEquals("group", label.getGroupName());
        assertEquals("name", label.getShortName());
        assertEquals("group" + delimiter + "name", label.getFullName());
        assertEquals(shouldBeExclusive, label.isInExclusiveGroup());

        // label format: group.
        label = new TurboLabel(REPO, "group" + delimiter);
        assertEquals("group", label.getGroupName());
        assertEquals("", label.getShortName());
        assertEquals("group" + delimiter, label.getFullName());
        assertEquals(shouldBeExclusive, label.isInExclusiveGroup());

        // The rest are unconditionally nonexlusive because there's no group.
        // The delimiter is taken to be part of the name instead.

        // label format: .name
        label = new TurboLabel(REPO, delimiter + "name");
        assertTrue(label.getGroupName().isEmpty());
        assertEquals(delimiter + "name", label.getFullName());
        assertFalse(label.isInExclusiveGroup());

        // label format: .
        label = new TurboLabel(REPO, delimiter);
        assertTrue(label.getGroupName().isEmpty());
        assertEquals(delimiter, label.getFullName());
        assertFalse(label.isInExclusiveGroup());
    }
}
