package util.events;

import ui.issuepanel.AbstractPanel;

public class RepoOpeningEvent extends Event {

    public final String repoId;
    public final boolean isPrimaryRepo;

    public RepoOpeningEvent(String repoId, boolean isPrimaryRepo) {
        this.repoId = repoId;
        this.isPrimaryRepo = isPrimaryRepo;
    }

}
