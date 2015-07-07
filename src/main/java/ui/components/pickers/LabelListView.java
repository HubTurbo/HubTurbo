package ui.components.pickers;

import ui.components.NavigableListView;

import java.util.Optional;

public class LabelListView extends NavigableListView<LabelPicker.Label> {

    private Optional<LabelPicker.Label> previousSelection = Optional.empty();
    private LabelPickerDialog labelPickerDialog;

    public LabelListView(LabelPickerDialog labelPickerDialog) {
        this.labelPickerDialog = labelPickerDialog;
    }

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
        setOnMouseClicked(e -> labelPickerDialog.requestFocus());
    }

    @Override
    protected void setupKeyEvents() {}

}
