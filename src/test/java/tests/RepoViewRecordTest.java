package tests;

import static org.junit.Assert.*;

import org.junit.Test;
import prefs.RepoViewRecord;

import java.time.LocalDateTime;

public class RepoViewRecordTest {

    LocalDateTime localDateTime = LocalDateTime.now();
    LocalDateTime localDateTime1 = LocalDateTime.of(1990, 10, 10, 10, 10, 10);
    RepoViewRecord repoViewRecord = new RepoViewRecord("dummy/dummy", localDateTime);
    RepoViewRecord repoViewRecord1 = new RepoViewRecord(null, localDateTime1);
    RepoViewRecord repoViewRecord2 = new RepoViewRecord("dummy/dummy", localDateTime);

    @Test
    public void fields() {
        assertEquals("dummy/dummy", repoViewRecord.getRepository());
        assertEquals(new RepoViewRecord("dummy/dummy").hashCode(), repoViewRecord.hashCode());
        assertEquals(localDateTime, repoViewRecord.getTimestamp());
        assertNotEquals(localDateTime, repoViewRecord1.getTimestamp());
        assertNotEquals(0, repoViewRecord.compareTo(repoViewRecord1));
    }

    @Test
    public void equality() {
        assertTrue(repoViewRecord.equals(repoViewRecord));
        assertFalse(repoViewRecord.equals(null));
        assertFalse(repoViewRecord.equals(""));
        assertFalse(repoViewRecord1.equals(repoViewRecord));
        assertTrue(repoViewRecord.equals(repoViewRecord2));
    }
}
