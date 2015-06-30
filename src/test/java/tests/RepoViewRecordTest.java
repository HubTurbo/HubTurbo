package tests;

import org.junit.Test;
import prefs.RepoViewRecord;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class RepoViewRecordTest {

    @Test
    public void repoViewRecordTest() {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime localDateTime1 = LocalDateTime.of(1990, 10, 10, 10, 10, 10);
        RepoViewRecord repoViewRecord = new RepoViewRecord("dummy/dummy");
        RepoViewRecord repoViewRecord1 = new RepoViewRecord(null);
        repoViewRecord.setTimestamp(localDateTime);
        repoViewRecord1.setTimestamp(localDateTime1);
        assertEquals("dummy/dummy", repoViewRecord.getRepository());
        assertEquals(new RepoViewRecord("dummy/dummy").hashCode(), repoViewRecord.hashCode());
        assertEquals(true, repoViewRecord.equals(repoViewRecord));
        assertEquals(false, repoViewRecord.equals(null));
        assertEquals(false, repoViewRecord.equals(""));
        assertEquals(false, repoViewRecord1.equals(repoViewRecord));
        assertEquals(localDateTime, repoViewRecord.getTimestamp());
        assertNotEquals(localDateTime, repoViewRecord1.getTimestamp());
    }

}
