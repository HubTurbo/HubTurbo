package util.events;

import backend.resource.TurboIssue;

public class ShowAssigneePickerEvent extends Event {

    public final TurboIssue issue;

    public ShowAssigneePickerEvent(TurboIssue issue) {
        this.issue = issue;
    }
}
