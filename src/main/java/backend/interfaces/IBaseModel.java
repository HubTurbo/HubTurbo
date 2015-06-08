package backend.interfaces;

import java.util.List;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;

public interface IBaseModel {
    List<TurboIssue> getIssues();
    List<TurboLabel> getLabels();
    List<TurboMilestone> getMilestones();
    List<TurboUser> getUsers();

    default String summarise() {
        return String.format("%d issue(s), %d label(s), %d milestone(s), %d user(s)",
            getIssues().size(),
            getLabels().size(),
            getMilestones().size(),
            getUsers().size());
    }
}
