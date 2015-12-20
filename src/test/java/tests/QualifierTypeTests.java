package tests;

import static filter.expression.QualifierType.*;
import static junit.framework.TestCase.*;

import java.util.Optional;
import java.util.Set;

import org.junit.Test;

public class QualifierTypeTests {

    @Test
    public void testCompletionKeywords() {

        Set<String> keywords = getCompletionKeywords();

        // Most qualifiers are represented
        assertTrue(keywords.contains("sort"));

        // Some qualifiers are deliberately excluded
        assertFalse(keywords.contains("is"));

        // Other non-qualifier keywords are included
        assertTrue(keywords.contains("repo"));
    }

    @Test
    public void testParse() {
        assertEquals(Optional.of(ASSIGNEE), parse("assignee"));
        assertFalse(parse("malformed").isPresent());
        assertFalse(parse("").isPresent());
        assertFalse(parse(null).isPresent());
    }

    @Test
    public void testStringify() {
        assertEquals("sort", SORT.toString());
    }
}
