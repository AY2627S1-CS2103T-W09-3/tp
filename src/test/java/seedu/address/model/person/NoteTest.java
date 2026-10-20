package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NoteTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Note(null));
    }

    @Test
    public void constructor_blankNote_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Note(""));
        assertThrows(IllegalArgumentException.class, () -> new Note("   "));
        assertThrows(IllegalArgumentException.class, () -> new Note("\n\t"));
    }

    @Test
    public void constructor_oneCharacter_success() {
        assertEquals("a", new Note("a").value);
    }

    @Test
    public void constructor_fiveHundredCharacters_success() {
        String note = "a".repeat(Note.MAX_LENGTH);
        assertEquals(note, new Note(note).value);
    }

    @Test
    public void constructor_fiveHundredOneCharacters_throwsIllegalArgumentException() {
        String note = "a".repeat(Note.MAX_LENGTH + 1);
        assertThrows(IllegalArgumentException.class, () -> new Note(note));
    }

    @Test
    public void constructor_realLineBreak_preserved() {
        String note = "First line\nSecond line";
        assertEquals(note, new Note(note).value);
    }

    @Test
    public void constructor_surroundingWhitespace_trimmed() {
        assertEquals("First line\nSecond line", new Note("  First line\nSecond line  ").value);
    }

    @Test
    public void constructor_escapedLineBreak_notDecoded() {
        String note = "First line\\nSecond line";
        assertEquals(note, new Note(note).value);
    }

    @Test
    public void isValidNote() {
        assertThrows(NullPointerException.class, () -> Note.isValidNote(null));

        assertFalse(Note.isValidNote(""));
        assertFalse(Note.isValidNote("   "));
        assertFalse(Note.isValidNote("a".repeat(Note.MAX_LENGTH + 1)));

        assertTrue(Note.isValidNote("a"));
        assertTrue(Note.isValidNote("a".repeat(Note.MAX_LENGTH)));
        assertTrue(Note.isValidNote("First line\nSecond line"));
        assertTrue(Note.isValidNote("  trimmed  "));
    }

    @Test
    public void equals() {
        Note note = new Note("Call after 6 PM");

        assertTrue(note.equals(new Note("Call after 6 PM")));
        assertEquals(note.hashCode(), new Note("Call after 6 PM").hashCode());
        assertTrue(note.equals(note));
        assertFalse(note.equals(null));
        assertFalse(note.equals(5.0f));
        assertFalse(note.equals(new Note("Call before 6 PM")));
    }
}
