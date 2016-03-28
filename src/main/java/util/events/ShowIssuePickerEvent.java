package util.events;

import java.util.List;

import backend.resource.TurboIssue;

/**
 * An event to show Issue Picker
 */
public class ShowIssuePickerEvent extends Event {

    public final boolean isStandalone;
    public final List<TurboIssue> allIssues;

    /**
     * @param isStandalone determines if the caller expects a return result
     *                     e.g when triggered by other UI element such as the filter text field
     */
    public ShowIssuePickerEvent(List<TurboIssue> allIssues, boolean isStandalone) {
        this.isStandalone = isStandalone;
        this.allIssues = allIssues;
    }
}
