package tests;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import org.junit.Test;
import ui.components.pickers.LabelPickerUILogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class LabelPickerLogicTests {

    public LabelPickerUILogic prepareLogic() {
        TurboIssue issue = new TurboIssue("dummy/dummy", 1, "Issue 1");
        return prepareLogic(issue);
    }

    public LabelPickerUILogic prepareLogic(TurboIssue issue) {
        ArrayList<TurboLabel> repoLabels = new ArrayList<>();
        repoLabels.add(new TurboLabel("dummy/dummy", "Label 1"));
        repoLabels.add(new TurboLabel("dummy/dummy", "Label 10"));
        repoLabels.add(new TurboLabel("dummy/dummy", "Label 11"));
        repoLabels.add(new TurboLabel("dummy/dummy", "p.low"));
        repoLabels.add(new TurboLabel("dummy/dummy", "p.mid"));
        repoLabels.add(new TurboLabel("dummy/dummy", "p.high"));
        repoLabels.add(new TurboLabel("dummy/dummy", "f-aaa"));
        repoLabels.add(new TurboLabel("dummy/dummy", "f-bbb"));
        repoLabels.add(new TurboLabel("dummy/dummy", "f-ccc"));
        repoLabels.add(new TurboLabel("dummy/dummy", "f-cdc"));
        repoLabels.add(new TurboLabel("dummy/dummy", "f-dcd"));
        repoLabels.add(new TurboLabel("dummy/dummy", "Priority.High"));
        repoLabels.add(new TurboLabel("dummy/dummy", "Priority.Mid"));
        repoLabels.add(new TurboLabel("dummy/dummy", "Priority.Low"));
        return new LabelPickerUILogic(issue, repoLabels);
    }

    public List<String> getLabels(LabelPickerUILogic logic) {
        return logic.getResultList().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Test
    public void groupTest() {
        // check for exclusive group toggling
        // we start with no labels
        LabelPickerUILogic logic = prepareLogic();
        assertEquals(0, getLabels(logic).size());
        // let's toggle two exclusive labels
        logic.toggleLabel("p.low");
        logic.toggleLabel("p.mid");
        // check to see that only one label has been applied
        assertEquals(1, getLabels(logic).size());
        assertEquals(true, logic.getResultList().get("p.mid"));
        // let's toggle two non-exclusive labels
        logic.toggleLabel("f-aaa");
        logic.toggleLabel("f-bbb");
        // so we should have 3 labels now
        assertEquals(3, getLabels(logic).size());

        // let's try starting with an issue with two exclusive labels in the same group
        TurboIssue issue = new TurboIssue("dummy/dummy", 1, "Issue 1");
        ArrayList<String> labels = new ArrayList<>();
        labels.add("p.low");
        labels.add("p.mid");
        issue.setLabels(labels);
        logic = prepareLogic(issue);
        // there should be two labels at first
        assertEquals(2, getLabels(logic).size());
        // we shall toggle one of the labels already in it
        logic.toggleLabel("p.mid");
        // we should be left with one label
        assertEquals(1, getLabels(logic).size());
        assertEquals(true, logic.getResultList().get("p.low"));

        // let's try starting with an issue with two exclusive labels in the same group
        issue = new TurboIssue("dummy/dummy", 1, "Issue 1");
        issue.setLabels(labels);
        logic = prepareLogic(issue);
        // there should be two labels at first
        assertEquals(2, getLabels(logic).size());
        // we shall toggle one of the labels in the same group but not in it
        logic.toggleLabel("p.high");
        // we should be left with one label
        assertEquals(1, getLabels(logic).size());
        assertEquals(true, logic.getResultList().get("p.high"));

        // starting with an issue with no labels
        logic = prepareLogic();
        assertEquals(0, getLabels(logic).size());
        // check for correct label selection
        logic.processTextFieldChange("f.d");
        assertEquals(true, logic.getHighlightedLabelName().isPresent());
        assertEquals("f-dcd", logic.getHighlightedLabelName().get().getActualName());
        // move highlight up once
        logic.moveHighlightOnLabel(false);
        assertEquals(true, logic.getHighlightedLabelName().isPresent());
        assertEquals("f-cdc", logic.getHighlightedLabelName().get().getActualName());
        // keep moving it upwards
        logic.moveHighlightOnLabel(false);
        logic.moveHighlightOnLabel(false);
        logic.moveHighlightOnLabel(false);
        assertEquals(true, logic.getHighlightedLabelName().isPresent());
        assertEquals("f-cdc", logic.getHighlightedLabelName().get().getActualName());

        // starting with an issue with no labels
        logic = prepareLogic();
        assertEquals(0, getLabels(logic).size());
        // check for correct label selection
        logic.processTextFieldChange("pri.h");
        assertEquals(true, logic.getHighlightedLabelName().isPresent());
        assertEquals("Priority.High", logic.getHighlightedLabelName().get().getActualName());
    }

    @Test
    public void moveHighlightTest() {
        // check for highlighted label movement
        LabelPickerUILogic logic = prepareLogic();
        assertEquals(0, getLabels(logic).size());
        // check for no highlight
        assertEquals(false, logic.getHighlightedLabelName().isPresent());
        // enter search query
        logic.processTextFieldChange("1");
        assertEquals(true, logic.getHighlightedLabelName().isPresent());
        assertEquals("Label 1", logic.getHighlightedLabelName().get().getActualName());
        // press down key
        logic.moveHighlightOnLabel(true);
        assertEquals(true, logic.getHighlightedLabelName().isPresent());
        assertEquals("Label 10", logic.getHighlightedLabelName().get().getActualName());
        // toggle label
        logic.toggleSelectedLabel("1 ");
        assertEquals(1, getLabels(logic).size());
        assertEquals(true, getLabels(logic).contains("Label 10"));
        assertEquals(false, logic.getHighlightedLabelName().isPresent());
        // enter search query and move down and up
        logic.processTextFieldChange("1 1");
        logic.moveHighlightOnLabel(true);
        logic.moveHighlightOnLabel(false);
        assertEquals(true, logic.getHighlightedLabelName().isPresent());
        assertEquals("Label 1", logic.getHighlightedLabelName().get().getActualName());
        // toggle label
        logic.toggleSelectedLabel("1 1 ");
        assertEquals(2, getLabels(logic).size());
        assertEquals(true, getLabels(logic).contains("Label 1"));
        assertEquals(false, logic.getHighlightedLabelName().isPresent());
    }

}
