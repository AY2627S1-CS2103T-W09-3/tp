package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class RemarkTest {
    @Test
    public void constructor_acceptsEmptyAndFreeText_rejectsNull() {
        assertEquals("", new Remark("").value);
        assertEquals("Likes tea! 茶", new Remark("Likes tea! 茶").toString());
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void equals_sameValue_sameHashCode() {
        Remark remark = new Remark("note");
        assertEquals(remark, new Remark("note"));
        assertEquals(remark.hashCode(), new Remark("note").hashCode());
        assertFalse(remark.equals(new Remark("other")));
        assertFalse(remark.equals(null));
        assertFalse(remark.equals("note"));
    }

    @Test
    public void person_differentRemark_changesEqualityButNotIdentity() {
        Person original = new PersonBuilder().build();
        Person updated = new PersonBuilder(original).withRemark("note").build();
        assertFalse(original.equals(updated));
        assertTrue(original.isSamePerson(updated));
    }
}
