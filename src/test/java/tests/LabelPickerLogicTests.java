package tests;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import org.junit.Test;
import ui.components.pickers.LabelPickerUILogic;
import ui.components.pickers.PickerLabel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        return logic.getActiveLabels().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public Method makeMethodAccessible(String methodName) throws NoSuchMethodException {
        Method getHighlightedLabelMethod =
                LabelPickerUILogic.class.getDeclaredMethod(methodName);
        getHighlightedLabelMethod.setAccessible(true);
        return getHighlightedLabelMethod;
    }

    @Test
    public void exclusiveGroupingTest() {
        // check for exclusive group toggling
        // we start with no labels
        LabelPickerUILogic logic = prepareLogic();
        assertEquals(0, getLabels(logic).size());
        // let's toggle two exclusive labels
        logic.toggleLabel("p.low");
        logic.toggleLabel("p.mid");
        // check to see that only one label has been applied
        assertEquals(1, getLabels(logic).size());
        assertEquals(true, logic.getActiveLabels().get("p.mid"));
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
        assertEquals(true, logic.getActiveLabels().get("p.low"));

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
        assertEquals(true, logic.getActiveLabels().get("p.high"));
    }

    @Test
    public void queryFilterTest() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method getHighlightedLabelMethod = makeMethodAccessible("getHighlightedLabel");

        // starting with an issue with no labels
        LabelPickerUILogic logic = prepareLogic();
        assertEquals(0, getLabels(logic).size());
        // check for correct label selection
        logic.processTextFieldChange("pri.h");
        assertEquals(true, invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).isPresent());
        assertEquals("Priority.High",
                invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).get().getActualName());


        logic = prepareLogic();
        // query highlights nothing
        logic.processTextFieldChange("asdf");
        assertEquals(false, logic.hasHighlightedLabel());
        // invalid space-toggle
        logic.processTextFieldChange("asdf ");
        assertEquals(0, getLabels(logic).size());
    }

    @Test
    public void backspaceTest() {
        // typical use case
        LabelPickerUILogic logic = prepareLogic();
        // query highlights Priority.Low
        logic.processTextFieldChange("p.l");
        // toggle Priority.Low
        logic.processTextFieldChange("p.l ");
        // query highlights Label1
        logic.processTextFieldChange("p.l label");
        // toggle Label1
        logic.processTextFieldChange("p.l label ");
        // backspace
        logic.processTextFieldChange("p.l label");
        assertEquals(1, getLabels(logic).size());
        assertEquals(true, logic.hasHighlightedLabel());
        // backspace until no query
        logic.processTextFieldChange("p.l ");
        assertEquals(1, getLabels(logic).size());
        assertEquals(false, logic.hasHighlightedLabel());
        // backspace
        logic.processTextFieldChange("p.l");
        assertEquals(0, getLabels(logic).size());
        assertEquals(true, logic.hasHighlightedLabel());
        logic.processTextFieldChange("");
        assertEquals(0, getLabels(logic).size());
        assertEquals(false, logic.hasHighlightedLabel());

        // let's test for restoring exclusive labels
        // start with an issue with no labels
        logic = prepareLogic();
        // query highlights Priority.Low
        logic.processTextFieldChange("p.l");
        // toggle Priority.Low
        logic.processTextFieldChange("p.l ");
        // query highlights Priority.Mid
        logic.processTextFieldChange("p.l p.m");
        // toggle exclusive label Priority.Mid, should only have 1 remaining
        logic.processTextFieldChange("p.l p.m ");
        // undo, Priority.Low should be restored automatically
        logic.processTextFieldChange("p.l p.m");
        assertEquals(1, getLabels(logic).size());
        assertEquals(true, getLabels(logic).contains("Priority.Low"));
        assertEquals(true, logic.hasHighlightedLabel());

        // try handling invalid label with backspacing
        // start with an issue with no labels
        logic = prepareLogic();
        // query highlights nothing
        logic.processTextFieldChange("asdf");
        // invalid space-toggle
        logic.processTextFieldChange("asdf ");
        // highlights Priority.High
        logic.processTextFieldChange("asdf p.h");
        // toggles Priority.High
        logic.processTextFieldChange("asdf p.h ");
        // untoggles Priority.High
        logic.processTextFieldChange("asdf p.h");
        assertEquals(0, getLabels(logic).size());
        assertEquals(true, logic.hasHighlightedLabel());
        // clears query
        logic.processTextFieldChange("asdf ");
        assertEquals(0, getLabels(logic).size());
        assertEquals(false, logic.hasHighlightedLabel());


    }

    @Test
    public void moveHighlightTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // check for highlighted label movement
        LabelPickerUILogic logic = prepareLogic();
        Method getHighlightedLabelMethod = makeMethodAccessible("getHighlightedLabel");

        assertEquals(0, getLabels(logic).size());
        // check for no highlight
        assertEquals(false, invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).isPresent());
        // enter search query
        logic.processTextFieldChange("1");
        assertEquals(true, invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).isPresent());
        assertEquals("Label 1",
                invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).get().getActualName());
        // press down key
        logic.moveHighlightOnLabel(true);
        assertEquals(true, invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).isPresent());
        assertEquals("Label 10",
                invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).get().getActualName());
        // toggle label
        logic.toggleHighlightedLabel();
        assertEquals(1, getLabels(logic).size());
        assertEquals(true, getLabels(logic).contains("Label 10"));
        assertEquals(false, invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).isPresent());
        // enter search query and move down and up
        logic.processTextFieldChange("1 1");
        logic.moveHighlightOnLabel(true);
        logic.moveHighlightOnLabel(false);
        assertEquals(true, invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).isPresent());
        assertEquals("Label 1",
                invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).get().getActualName());
        // toggle label
        logic.toggleHighlightedLabel();
        assertEquals(2, getLabels(logic).size());
        assertEquals(true, getLabels(logic).contains("Label 1"));
        assertEquals(false, invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).isPresent());

        // starting with an issue with no labels
        logic = prepareLogic();
        assertEquals(0, getLabels(logic).size());
        // check for correct label selection
        logic.processTextFieldChange("f.d");
        assertEquals(true, invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).isPresent());
        assertEquals("f-dcd",
                invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).get().getActualName());
        // move highlight up once
        logic.moveHighlightOnLabel(false);
        assertEquals(true, invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).isPresent());
        assertEquals("f-cdc",
                invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).get().getActualName());
        // keep moving it upwards
        logic.moveHighlightOnLabel(false);
        logic.moveHighlightOnLabel(false);
        logic.moveHighlightOnLabel(false);
        assertEquals(true, invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).isPresent());
        assertEquals("f-cdc",
                invokeGetHighlightedLabelMethod(getHighlightedLabelMethod, logic).get().getActualName());
    }

    @Test
    public void mixedSeparatorsTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getExistingLabelsMethod = makeMethodAccessible("getExistingLabels");
        Method getAddedLabelsMethod = makeMethodAccessible("getAddedLabels");

        // we start off with an issue with two labels
        TurboIssue issue = new TurboIssue("dummy/dummy", 1, "Issue 1");
        ArrayList<String> labels = new ArrayList<>();
        labels.add("f-aaa");
        labels.add("p.low");
        issue.setLabels(labels);
        LabelPickerUILogic logic = prepareLogic(issue);
        // check for two existing labels
        assertEquals(2, invokePickerLabelListMethod(getExistingLabelsMethod, logic).size());
        assertEquals(0, invokePickerLabelListMethod(getAddedLabelsMethod, logic).size());
        // we potentially add one more (note that the group delimiter does not matter)
        logic.processTextFieldChange("f.b");
        assertEquals(2, invokePickerLabelListMethod(getExistingLabelsMethod, logic).size());
        assertEquals(1, invokePickerLabelListMethod(getAddedLabelsMethod, logic).size());
        // we empty the text field without toggling the label to see if it resets back
        logic.processTextFieldChange("");
        assertEquals(2, invokePickerLabelListMethod(getExistingLabelsMethod, logic).size());
        assertEquals(0, invokePickerLabelListMethod(getAddedLabelsMethod, logic).size());
        // we toggle a new label
        logic.toggleLabel("f-bbb");
        assertEquals(2, invokePickerLabelListMethod(getExistingLabelsMethod, logic).size());
        assertEquals(1, invokePickerLabelListMethod(getAddedLabelsMethod, logic).size());
        // we potentially add a non-existent label
        logic.processTextFieldChange("abcdefg");
        assertEquals(2, invokePickerLabelListMethod(getExistingLabelsMethod, logic).size());
        assertEquals(1, invokePickerLabelListMethod(getAddedLabelsMethod, logic).size());
        // we toggle an existing label
        logic.toggleLabel("f-aaa");
        assertEquals(2, invokePickerLabelListMethod(getExistingLabelsMethod, logic).size());
        assertEquals(1, invokePickerLabelListMethod(getAddedLabelsMethod, logic).size());
        // we toggle a new exclusive label
        logic.toggleLabel("p.mid");
        assertEquals(2, invokePickerLabelListMethod(getExistingLabelsMethod, logic).size());
        assertEquals(2, invokePickerLabelListMethod(getAddedLabelsMethod, logic).size());
        // we toggle yet another new exclusive label
        logic.toggleLabel("p.high");
        assertEquals(2, invokePickerLabelListMethod(getExistingLabelsMethod, logic).size());
        assertEquals(2, invokePickerLabelListMethod(getAddedLabelsMethod, logic).size());
    }

    @SuppressWarnings("unchecked")
    private Optional<PickerLabel> invokeGetHighlightedLabelMethod
            (Method getHighlightedLabelMethod, LabelPickerUILogic logic)
            throws InvocationTargetException, IllegalAccessException {
        return (Optional<PickerLabel>) getHighlightedLabelMethod.invoke(logic);
    }

    @SuppressWarnings("unchecked")
    private List<PickerLabel> invokePickerLabelListMethod(Method getPickerLabelListMethod, LabelPickerUILogic logic)
            throws InvocationTargetException, IllegalAccessException {
        return (List<PickerLabel>) getPickerLabelListMethod.invoke(logic);
    }

}
