package util.events;

import backend.resource.TurboIssue;

public class ShowLabelPickerEvent extends Event {

    public final TurboIssue issue;

    public ShowLabelPickerEvent(TurboIssue issue) {
        this.issue = issue;
    }

}
