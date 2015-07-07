package ui.components.pickers;

import ui.components.NavigableListView;

import java.util.Optional;

public class LabelListView extends NavigableListView<LabelPicker.Label> {

    private Optional<LabelPicker.Label> previousSelection = Optional.empty();

    public boolean areItemsEqual(LabelPicker.Label item1, LabelPicker.Label item2) {
        return item1.getName().equals(item2.getName());
    }

    public void toggleSelectedItem() {
        if (getSelectedItem().isPresent()) {
            getSelectedItem().get().toggleChecked();
        }
    }

    public void showSelectedItemChange() {
        if (getSelectedItem().isPresent()) {
            if (previousSelection.isPresent()) {
                previousSelection.get().setSelected(false);
            }
            getSelectedItem().get().setSelected(true);
            previousSelection = Optional.of(getSelectedItem().get());
        }
    }

    public void setFirstItem() {
        if (getItems().size() == 0) return;
        getSelectionModel().clearAndSelect(0);
        scrollAndShow(0);
        selectedIndex = Optional.of(0);
        showSelectedItemChange();
    }

    @Override
    protected void setupMouseEvents() {
        setOnMouseClicked(e -> {
            int currentlySelected = getSelectionModel().getSelectedIndex();

            // The currently-selected index is sometimes -1 when an issue is clicked.
            // When this happens we ignore this event.

            if (currentlySelected != -1) {
                selectedIndex = Optional.of(currentlySelected);
                toggleSelectedItem();
            }
        });
    }

}
