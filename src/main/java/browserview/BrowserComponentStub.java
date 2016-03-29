package browserview;

import com.sun.jna.platform.win32.WinDef;
import ui.UI;

public class BrowserComponentStub extends BrowserComponent {

    public BrowserComponentStub(UI ui) {
        super(ui, null, false);
    }

    @Override
    public void onAppQuit() {
    }

    @Override
    public void focus(WinDef.HWND mainWindowHandle) {
    }

    @Override
    public boolean hasBviewChanged() {
        return false;
    }

    @Override
    public void newLabel() {
    }

    @Override
    public void newMilestone() {
    }

    @Override
    public void newIssue() {
    }

    @Override
    public void showIssue(String repoId, int id, boolean isPullRequest, boolean isForceRefresh) {
    }

    @Override
    public boolean isCurrentUrlIssue() {
        return true;
    }

    @Override
    public String getCurrentUrl() {
        return "https://github.com/HubTurbo/HubTurbo/issues/1";
    }

}
