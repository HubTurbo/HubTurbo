package ui.components;

import backend.resource.TurboIssue;

public class IssueListView extends NavigableListView<TurboIssue> {

    boolean areItemsEqual(TurboIssue item1, TurboIssue item2) {
        return item1.getId() == item2.getId() &&
                item1.getCreator().equals(item2.getCreator()) &&
                item1.isPullRequest() == item2.isPullRequest();
    }

}
