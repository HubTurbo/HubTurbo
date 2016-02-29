package util.events;

import java.util.Optional;

import backend.resource.TurboIssue;

public class ShowIssueCreatorEvent extends Event {

    public final Optional<TurboIssue> issue;

    public ShowIssueCreatorEvent(Optional<TurboIssue> issue) {
        this.issue = issue;
    }

}
