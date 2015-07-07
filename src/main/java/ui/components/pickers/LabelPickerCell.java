package ui.components.pickers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * Mostly a copy of CheckBoxListCell
 */

public class LabelPickerCell extends ListCell<LabelPicker.Label> {

    private final CheckBox checkBox;
    private ObservableValue<Boolean> booleanProperty;
    private ObjectProperty<Callback<LabelPicker.Label, ObservableValue<Boolean>>>
            selectedStateCallback =
            new SimpleObjectProperty<>(
                    this, "selectedStateCallback");
    private ObjectProperty<StringConverter<LabelPicker.Label>> converter =
            new SimpleObjectProperty<>(this, "converter");
    private LabelPicker.Label item;

    public static Callback<ListView<LabelPicker.Label>, ListCell<LabelPicker.Label>> forListView(
            final Callback<LabelPicker.Label, ObservableValue<Boolean>> getSelectedProperty,
            final StringConverter<LabelPicker.Label> converter) {
        return list -> new LabelPickerCell(getSelectedProperty, converter);
    }

    public LabelPickerCell(
            final Callback<LabelPicker.Label, ObservableValue<Boolean>> getSelectedProperty,
            final StringConverter<LabelPicker.Label> converter) {
        this.getStyleClass().add("check-box-list-cell");
        setSelectedStateCallback(getSelectedProperty);
        setConverter(converter);

        this.checkBox = new CheckBox();

        setAlignment(Pos.CENTER_LEFT);
        setContentDisplay(ContentDisplay.LEFT);

        // by default the graphic is null until the cell stops being empty
        setGraphic(null);

        setOnMouseClicked(e -> {
            item.toggleChecked();
            updateItem(item, false);
        });
    }

    @Override public void updateItem(LabelPicker.Label item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty) {
            this.item = item;
            StringConverter<LabelPicker.Label> c = getConverter();
            Callback<LabelPicker.Label, ObservableValue<Boolean>> callback = getSelectedStateCallback();
            if (callback == null) {
                throw new NullPointerException(
                        "The CheckBoxListCell selectedStateCallbackProperty can not be null");
            }

            setGraphic(checkBox);
            if (item != null && item.isSelected()) {
                setText(c != null ? "▶ " + c.toString(item) : item.toString());
            } else {
                setText(c != null ? c.toString(item) : (item == null ? "" : item.toString()));
            }
            setStyle(item != null ? item.getStyle() : null);

            if (booleanProperty != null) {
                checkBox.selectedProperty().unbindBidirectional((BooleanProperty) booleanProperty);
            }
            booleanProperty = callback.call(item);
            if (booleanProperty != null) {
                checkBox.selectedProperty().bindBidirectional((BooleanProperty) booleanProperty);
            }
        } else {
            setGraphic(null);
            setText(null);
        }
    }

    public final ObjectProperty<StringConverter<LabelPicker.Label>> converterProperty() {
        return converter;
    }

    public final void setSelectedStateCallback(Callback<LabelPicker.Label, ObservableValue<Boolean>> value) {
        selectedStateCallbackProperty().set(value);
    }

    public final void setConverter(StringConverter<LabelPicker.Label> value) {
        converterProperty().set(value);
    }

    public final ObjectProperty<Callback<LabelPicker.Label,
            ObservableValue<Boolean>>> selectedStateCallbackProperty() {
        return selectedStateCallback;
    }

    public final StringConverter<LabelPicker.Label> getConverter() {
        return converterProperty().get();
    }

    public final Callback<LabelPicker.Label, ObservableValue<Boolean>> getSelectedStateCallback() {
        return selectedStateCallbackProperty().get();
    }

}
