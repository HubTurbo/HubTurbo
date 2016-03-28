package util;

import static ui.components.KeyboardShortcuts.GLOBAL_HOTKEY;

import javafx.application.Platform;

import javax.swing.KeyStroke;

import ui.UI;

import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;

public class GlobalHotkey {
    private final Provider provider = Provider.getCurrentProvider(false);
    private final UI ui;

    public GlobalHotkey(UI ui) {
        this.ui = ui;
    }

    public void init() {
        provider.register(KeyStroke.getKeyStroke(GLOBAL_HOTKEY), new HotKeyListener() {
            public void onHotKey(HotKey hotKey) {
                if (!ui.isWindowMinimized() && ui.isWindowFocused()) {
                    Platform.runLater(() -> ui.minimizeWindow());
                    ui.getBrowserComponent().minimizeWindow();
                } else {
                    Platform.runLater(() -> ui.setDefaultWidth());
                    ui.getBrowserComponent().focus(ui.getMainWindowHandle());
                }
            }
        });
    }

    public void quit() {
        provider.reset();
        provider.stop();
    }
}
