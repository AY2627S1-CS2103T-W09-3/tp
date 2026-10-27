package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));
    }

    @Test
    public void messageConstraints_hasExpectedWording() {
        assertEquals("Tag names should be alphanumeric", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void equals() {
        Tag tag = new Tag("urgent");

        assertTrue(tag.equals(tag));
        assertTrue(tag.equals(new Tag("urgent")));
        assertTrue(tag.equals(new Tag("URGENT")));
        assertEquals(tag.hashCode(), new Tag("URGENT").hashCode());
        assertFalse(tag.equals(new Tag("important")));
        assertFalse(tag.equals(null));
        assertFalse(tag.equals("urgent"));
    }
}
