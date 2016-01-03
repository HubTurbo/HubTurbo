package undo;

import backend.resource.TurboIssue;

import java.util.ArrayList;
import java.util.List;

public class ChangeLabels implements Action<TurboIssue> {

    private final List<String> addedLabels;
    private final List<String> removedLabels;

    public ChangeLabels(List<String> addedLabels, List<String> removedLabels) {
        this.addedLabels = new ArrayList<>(addedLabels);
        this.removedLabels = new ArrayList<>(removedLabels);
    }

    @Override
    public TurboIssue act(TurboIssue turboIssue) {
        List<String> newLabels = new ArrayList<>(turboIssue.getLabels());
        newLabels.addAll(addedLabels);
        newLabels.removeAll(removedLabels);
        TurboIssue newTurboIssue = new TurboIssue(turboIssue);
        newTurboIssue.setLabels(newLabels);
        return newTurboIssue;
    }

    @Override
    public TurboIssue undo(TurboIssue turboIssue) {
        List<String> newLabels = new ArrayList<>(turboIssue.getLabels());
        newLabels.addAll(removedLabels);
        newLabels.removeAll(addedLabels);
        TurboIssue newTurboIssue = new TurboIssue(turboIssue);
        newTurboIssue.setLabels(newLabels);
        return newTurboIssue;
    }

}
