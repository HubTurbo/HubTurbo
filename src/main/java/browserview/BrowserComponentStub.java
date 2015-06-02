package browserview;

import com.sun.jna.platform.win32.WinDef;
import ui.UI;

public class BrowserComponentStub extends BrowserComponent {

    public BrowserComponentStub(UI ui) {
        super(ui, false);
    }

    @Override
    public void onAppQuit() {}

    @Override
    public void focus(WinDef.HWND mainWindowHandle) {}

    @Override
    public boolean hasBviewChanged() { return false; }

    @Override
    public void newLabel() {}

    @Override
    public void newMilestone() {}

    @Override
    public void newIssue() {}

    @Override
    public void showDocs() {}

    @Override
    public void showChangelog(String version) {}

    @Override
    public void showIssue(String repoId, int id) {}

    @Override
    public void jumpToComment(){}

    @Override
    public void login() {}

    @Override
    public void scrollToTop() {}

    @Override
    public void scrollToBottom() {}

    @Override
    public void scrollPage(boolean isDownScroll) {}

    @Override
    public void manageLabels(String keyCode) {}

    @Override
    public void manageAssignees(String keyCode) {}

    @Override
    public void manageMilestones(String keyCode) {}

    @Override
    public void showIssues() {}

    @Override
    public void showPullRequests() {}

    @Override
    public void showKeyboardShortcuts() {}

    @Override
    public void showMilestones() {}

    @Override
    public void showContributors() {}

    @Override
    public boolean isCurrentUrlIssue() { return true; }
}
