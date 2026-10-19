package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a note attached to a client.
 * Guarantees: immutable; contains 1 to 500 characters after surrounding whitespace is trimmed.
 * Escape sequences are not decoded here; input parsers must decode them before constructing a note.
 */
public class Note {

    public static final String MESSAGE_CONSTRAINTS = "Notes should be 1 to 500 characters.";
    public static final int MAX_LENGTH = 500;

    public final String value;

    /**
     * Constructs a {@code Note}.
     *
     * @param note A valid note.
     */
    public Note(String note) {
        requireNonNull(note);
        String trimmedNote = note.trim();
        checkArgument(isValidNote(trimmedNote), MESSAGE_CONSTRAINTS);
        value = trimmedNote;
    }

    /**
     * Returns true if a given string is a valid note.
     */
    public static boolean isValidNote(String test) {
        requireNonNull(test);
        String trimmedNote = test.trim();
        return !trimmedNote.isEmpty() && trimmedNote.length() <= MAX_LENGTH;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Note otherNote)) {
            return false;
        }
        return value.equals(otherNote.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
