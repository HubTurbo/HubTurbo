package tests;

import backend.resource.MultiModel;
import org.junit.Test;
import prefs.Preferences;

import static org.junit.Assert.assertEquals;

public class MultiModelTest {

    @Test
    public void multiModelTest() {
        MultiModel multiModel = new MultiModel(new Preferences(true));
        assertEquals(true, multiModel.equals(multiModel));
        assertEquals(false, multiModel.equals(null));
        assertEquals(false, multiModel.equals(""));
        assertEquals(new MultiModel(new Preferences(true)), multiModel);
        assertEquals(new MultiModel(new Preferences(true)).hashCode(), multiModel.hashCode());
    }

}
