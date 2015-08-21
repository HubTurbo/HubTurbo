package tests;

import backend.resource.MultiModel;
import org.junit.Test;
import prefs.Preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MultiModelTest {

    MultiModel multiModel = new MultiModel(new Preferences(true));

    @Test
    public void equality() {
        assertTrue(multiModel.equals(multiModel));
        assertFalse(multiModel.equals(null));
        assertFalse(multiModel.equals(""));
    }

    @Test
    public void multiModelTest() {
        assertEquals(new MultiModel(new Preferences(true)), multiModel);
        assertEquals(new MultiModel(new Preferences(true)).hashCode(), multiModel.hashCode());
    }

}
