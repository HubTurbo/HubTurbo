package util.events;

import backend.resource.TurboIssue;

public class ShowMilestonePickerEvent extends Event {

    public final TurboIssue issue;

    public ShowMilestonePickerEvent(TurboIssue issue) {
        this.issue = issue;
    }

}
