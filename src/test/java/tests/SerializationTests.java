package tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import backend.resource.TurboLabel;
import backend.resource.serialization.SerializableLabel;

public class SerializationTests {

    @Test
    public void testSerializableLabelNoColorColorToString() {
        TurboLabel label = new TurboLabel("dummy/dummy", "label.name");
        SerializableLabel serializedLabel = new SerializableLabel(label);

        assertEquals(serializedLabel.toString(), "Label: {name: label.name, color: ffffff}");
    }

    @Test
    public void testSerializableLabelWithColorToString() {
        TurboLabel label = new TurboLabel("dummy/dummy", "abcdef", "label.name");
        SerializableLabel serializedLabel = new SerializableLabel(label);

        assertEquals(serializedLabel.toString(), "Label: {name: label.name, color: abcdef}");
    }
}
