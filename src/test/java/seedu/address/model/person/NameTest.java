package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }

    @Test
    public void messageConstraints_hasExpectedWording() {
        assertEquals("Names must start with a letter or number and contain only letters, numbers, spaces, "
                + "apostrophes (' or ’), hyphens (-), or slashes (/).",
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void isValidName_commonNameForms_returnsTrue() {
        for (String name : List.of("Anne-Marie Tan", "José Tan", "Jose\u0301 Tan", "D'Arcy Tan", "D’Arcy Tan",
                "Kumar s/o Ravi", "Devi d/o Ravi", "Arun a/l Mani", "Devi a/p Mani", "陈伟", "அருண்")) {
            assertTrue(Name.isValidName(name), name);
        }
    }

    @Test
    public void isValidName_unsupportedCharacters_returnsFalse() {
        for (String name : List.of("-", "/", "'", "’", "\u0301Jose", "Anne_Marie", "José@", "José\tTan",
                "José\nTan", " Anne-Marie")) {
            assertFalse(Name.isValidName(name), name);
        }
    }

    @Test
    public void constructor_canonicallyEquivalentNames_normalizesUnicode() {
        Name composed = new Name("José Tan");
        Name decomposed = new Name("Jose\u0301 Tan");

        assertEquals("José Tan", decomposed.fullName);
        assertEquals(composed, decomposed);
        assertEquals(composed.hashCode(), decomposed.hashCode());
        assertTrue(composed.isSameName(decomposed));
    }

    @Test
    public void isSameName() {
        Name name = new Name("Valid Name");

        // same value -> returns true
        assertTrue(name.isSameName(new Name("Valid Name")));

        // differs in case -> returns true
        assertTrue(name.isSameName(new Name("vALID nAME")));

        // null -> returns false
        assertFalse(name.isSameName(null));

        // different name -> returns false
        assertFalse(name.isSameName(new Name("Other Valid Name")));

        // trailing space -> returns false
        assertFalse(name.isSameName(new Name("Valid Name ")));

        // extra internal space -> returns false
        assertFalse(name.isSameName(new Name("Valid  Name")));
    }
}
