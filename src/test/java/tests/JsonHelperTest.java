package tests;

import org.junit.Test;
import static org.junit.Assert.*;
import util.JsonHelper;

import java.util.Arrays;

public class JsonHelperTest {

    /**
     * Dummy class to test json conversion
     */
    private static class Dummy {
        // default values
        private int testInt = 3;
        private boolean testBoolean = false;
        private String testString = "test";
        private SecondDummy testSecondDummy = new SecondDummy();

        public int getTestInt() {
            return testInt;
        }

        public void setTestInt(int testInt) {
            this.testInt = testInt;
        }

        public boolean getTestBoolean() {
            return testBoolean;
        }

        public void setTestBoolean(boolean testBoolean) {
            this.testBoolean = testBoolean;
        }

        public String getTestString() {
            return testString;
        }

        public void setTestString(String testString) {
            this.testString = testString;
        }

        public SecondDummy getTestSecondDummy() {
            return testSecondDummy;
        }

        public void setTestSecondDummy(SecondDummy testSecondDummy) {
            this.testSecondDummy = testSecondDummy;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o instanceof Dummy) {
                Dummy od = (Dummy) o;
                return this.testInt == od.testInt &&
                        this.testBoolean == od.testBoolean &&
                        this.testString.equals(od.testString) &&
                        this.testSecondDummy.equals(od.testSecondDummy);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            // Does not support hashcode
            // This practice is recommended by findbugs
            assert false;
            return 0;
        }
    }

    /**
     * Dummy class to test json conversion
     */
    private static class SecondDummy {
        // default values
        private int[] testIntArr = {0, 1, 2};

        public int[] getTestIntArr() {
            return testIntArr;
        }

        public void setTestIntArr(int... testIntArr) {
            this.testIntArr = testIntArr;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o instanceof SecondDummy) {
                SecondDummy od = (SecondDummy) o;
                return Arrays.equals(this.testIntArr, od.testIntArr);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            // Does not support hashcode
            // This practice is recommended by findbugs
            assert false;
            return 0;
        }
    }

    @Test
    public void fromJson_emptyString_nullObject() {
        String emptyString = "";
        Dummy dummy = JsonHelper.fromJsonString(emptyString, Dummy.class);
        assertEquals(null, dummy);
    }

    @Test
    public void fromJson_nullJson_nullObject() {
        String emptyJson = "null";
        Dummy dummy = JsonHelper.fromJsonString(emptyJson, Dummy.class);
        assertEquals(null, dummy);
    }

    @Test
    public void fromJson_emptyJson_defaultObject() {
        String emptyJson = "{}";
        Dummy dummy = JsonHelper.fromJsonString(emptyJson, Dummy.class);
        assertEquals(new Dummy(), dummy);
    }

    @Test
    public void fromJson_constructedJson_constructedObject() {
        String constructedJson = "{" +
                "\"testInt\":42," +
                "\"testBoolean\":true," +
                "\"testString\":\"expected\"," +
                "\"testSecondDummy\":{\"testIntArr\":[4,2]}" +
                "}";
        Dummy dummy = JsonHelper.fromJsonString(constructedJson, Dummy.class);
        Dummy expectedDummy = new Dummy();
        SecondDummy expectedSecondDummy = new SecondDummy();
        expectedSecondDummy.setTestIntArr(new int[]{4, 2});
        expectedDummy.setTestBoolean(true);
        expectedDummy.setTestInt(42);
        expectedDummy.setTestString("expected");
        expectedDummy.setTestSecondDummy(expectedSecondDummy);
        assertNotEquals(null, dummy);
        assertEquals(expectedDummy, dummy);
    }

    @Test
    public void toJson_nullObject_nullJson() {
        String json = JsonHelper.toJsonString(null, Dummy.class);
        assertEquals("null", json);
    }

    @Test
    public void toJson_defaultObject_defaultJson() {
        Dummy dummyForJson = new Dummy();
        String json = JsonHelper.toJsonString(dummyForJson, Dummy.class).replaceAll("\\s+", "");
        String expectedJson = "{" +
                "\"testInt\":3," +
                "\"testBoolean\":false," +
                "\"testString\":\"test\"," +
                "\"testSecondDummy\":{\"testIntArr\":[0,1,2]}" +
                "}";
        assertEquals(expectedJson, json);
    }

    @Test
    public void toJson_constructedObject_constructedJson() {
        Dummy dummyForJson = new Dummy();
        SecondDummy secondDummy = new SecondDummy();
        secondDummy.setTestIntArr(new int[]{4, 2});
        dummyForJson.setTestSecondDummy(secondDummy);
        dummyForJson.setTestInt(42);
        dummyForJson.setTestBoolean(true);
        dummyForJson.setTestString("expected");
        String json = JsonHelper.toJsonString(dummyForJson, Dummy.class).replaceAll("\\s", "");
        String expectedJson = "{" +
                "\"testInt\":42," +
                "\"testBoolean\":true," +
                "\"testString\":\"expected\"," +
                "\"testSecondDummy\":{\"testIntArr\":[4,2]}" +
                "}";
        assertEquals(expectedJson, json);
    }
}
