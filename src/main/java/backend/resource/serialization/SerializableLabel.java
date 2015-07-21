package backend.resource.serialization;

import backend.resource.TurboLabel;

public class SerializableLabel {

    private String actualName = "";
    private String colour = "";

    public SerializableLabel(TurboLabel label) {
        this.actualName = label.getActualName();
        this.colour = label.getColour();
    }

    public String getActualName() {
        return actualName;
    }

    public String getColour() {
        return colour;
    }

    @Override
    public String toString() {
        String formatter = "Label: {name: %s, color: %s}";
        return String.format(formatter, getActualName(), getColour());
    }
}
