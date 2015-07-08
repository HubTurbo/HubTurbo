package ui.components.pickers;

import backend.resource.TurboLabel;

public class PickerLabel extends TurboLabel {

    public PickerLabel(TurboLabel label) {
        super(label.getRepoId(), label.getColour(), label.getActualName());
    }

}
