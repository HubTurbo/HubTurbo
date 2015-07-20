package ui.components;

import backend.resource.TurboIssue;

public class IssueListView extends NavigableListView<TurboIssue> {

    public boolean areItemsEqual(TurboIssue item1, TurboIssue item2) {
        return item1.getId() == item2.getId() &&
                item1.getRepoId().equals(item2.getRepoId());
    }

}
