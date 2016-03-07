package util.events;

import backend.resource.TurboIssue;

public class ShowCommentViewerEvent extends Event {

    public final TurboIssue issue;

    public ShowCommentViewerEvent(TurboIssue issue) {
        this.issue = issue;
    }

}
