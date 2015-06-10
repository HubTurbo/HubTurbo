package tests;

import org.junit.Test;
import prefs.RepoViewRecord;

import static org.junit.Assert.assertEquals;

public class RepoViewRecordTest {

    @Test
    public void repoViewRecordTest() {
        RepoViewRecord repoViewRecord = new RepoViewRecord("dummy/dummy");
        RepoViewRecord repoViewRecord1 = new RepoViewRecord(null);
        assertEquals("dummy/dummy", repoViewRecord.getRepository());
        assertEquals(new RepoViewRecord("dummy/dummy").hashCode(), repoViewRecord.hashCode());
        assertEquals(true, repoViewRecord.equals(repoViewRecord));
        assertEquals(false, repoViewRecord.equals(null));
        assertEquals(false, repoViewRecord.equals(""));
        assertEquals(false, repoViewRecord1.equals(repoViewRecord));
    }

}
