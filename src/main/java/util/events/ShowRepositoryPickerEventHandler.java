package util.events;

import com.google.common.eventbus.Subscribe;

/**
 * Handles ShowRepositoryPickerEvent by displaying RepositoryPicker
 */
@FunctionalInterface
public interface ShowRepositoryPickerEventHandler extends EventHandler {
    @Subscribe
    void handle(ShowRepositoryPickerEvent e);
}
