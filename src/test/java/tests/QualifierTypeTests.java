package tests;

import static filter.expression.QualifierType.*;
import static junit.framework.TestCase.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;

import filter.expression.QualifierType;

public class QualifierTypeTests {

    @Test
    public void testCompletionKeywords() {

        Set<String> keywords = getCompletionKeywords();

        // Most qualifiers are represented
        assertTrue(keywords.contains("sort"));

        // Some qualifiers are deliberately excluded
        assertFalse(keywords.contains("a"));

        // Other non-qualifier keywords are included
        assertTrue(keywords.contains("repo"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAliases() throws IllegalAccessException {
        Map<String, QualifierType> aliases =
                (Map<String, QualifierType>)
                        FieldUtils.readDeclaredStaticField(QualifierType.class, "ALIASES", true);

        // Check that every alias resolves to its declared equivalent
        for (Map.Entry<String, QualifierType> entry : aliases.entrySet()) {
            assertEquals(Optional.of(entry.getValue()), parse(entry.getKey()));
        }
    }

    @Test
    public void testParse() {
        assertEquals(Optional.of(ASSIGNEE), parse("assignee"));
        assertFalse(parse("malformed").isPresent());
        assertFalse(parse("").isPresent());
        assertFalse(parse(null).isPresent());

        // Special qualifiers will not parse
        assertFalse(parse("empty").isPresent());
        assertFalse(parse("false").isPresent());
        assertFalse(parse("keyword").isPresent());
    }

    @Test
    public void testStringify() {
        assertEquals("sort", SORT.toString());
    }
}
