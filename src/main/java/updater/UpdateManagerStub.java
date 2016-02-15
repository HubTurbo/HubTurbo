package updater;

/**
 * Stub for UpdateManager to be used in test mode
 */
public class UpdateManagerStub extends UpdateManager {
    public UpdateManagerStub() {
        super(null, null);
    }

    @Override
    public void run() {
        // This method is intentionally left empty
    }

    @Override
    public void showUpdateProgressWindow() {
        // This method is intentionally left empty
    }

    @Override
    public void hideUpdateProgressWindow() {
        // This method is intentionally left empty
    }
}
