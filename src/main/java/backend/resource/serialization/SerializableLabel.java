package backend.resource.serialization;

import backend.resource.TurboLabel;

/**
 * Warnings are suppressed to prevent complaints about fields not being final.
 * They are this way to give them default values.
 */
@SuppressWarnings("PMD")
public class SerializableLabel {

    private String fullName = "";
    private String colour = "";

    public SerializableLabel(TurboLabel label) {
        this.fullName = label.getFullName();
        this.colour = label.getColour();
    }

    public String getFullName() {
        return fullName;
    }

    public String getColour() {
        return colour;
    }
}
