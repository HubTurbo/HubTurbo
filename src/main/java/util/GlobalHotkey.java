package util;

import static ui.components.KeyboardShortcuts.GLOBAL_HOTKEY;

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
                ui.getBrowserComponent().focus(ui.getMainWindowHandle());
            }
        });
    }

    public void quit() {
        provider.reset();
        provider.stop();
    }
}
