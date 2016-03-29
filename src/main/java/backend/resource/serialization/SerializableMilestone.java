package backend.resource.serialization;

import backend.resource.TurboMilestone;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Warnings are suppressed to prevent complaints about fields not being final.
 * They are this way to give them default values.
 */
@SuppressWarnings("PMD")
public class SerializableMilestone {
    private int id;
    private String title;
    private Optional<LocalDate> dueDate;
    private String description;
    private boolean isOpen;
    private int openIssues;
    private int closedIssues;

    public SerializableMilestone(TurboMilestone milestone) {
        this.id = milestone.getId();
        this.title = milestone.getTitle();
        this.dueDate = milestone.getDueDate();
        this.description = milestone.getDescription();
        this.isOpen = milestone.isOpen();
        this.openIssues = milestone.getOpenIssues();
        this.closedIssues = milestone.getClosedIssues();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Optional<LocalDate> getDueDate() {
        return dueDate;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public int getOpenIssues() {
        return openIssues;
    }

    public int getClosedIssues() {
        return closedIssues;
    }
}
