package backend.resource.serialization;

import backend.resource.TurboLabel;

/**
 * Warnings are suppressed to prevent complaints about fields not being final.
 * They are this way to give them default values.
 */
@SuppressWarnings("PMD")
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
}
