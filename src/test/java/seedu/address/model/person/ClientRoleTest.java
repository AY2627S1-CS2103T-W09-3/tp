package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;


public class ClientRoleTest {

    @Test
    public void isValidRole_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ClientRole.isValidRole(null));
    }

    @Test
    public void isValidRole_validRole_returnsTrue() {
        assertTrue(ClientRole.isValidRole("buyer"));
        assertTrue(ClientRole.isValidRole("seller"));
        assertTrue(ClientRole.isValidRole("both"));

        // matching ignores case
        assertTrue(ClientRole.isValidRole("BUYER"));
        assertTrue(ClientRole.isValidRole("Both"));
        assertTrue(ClientRole.isValidRole("SeLlEr"));
    }

    @Test
    public void isValidRole_invalidRole_returnsFalse() {
        assertFalse(ClientRole.isValidRole("")); // empty string
        assertFalse(ClientRole.isValidRole(" ")); // spaces only
        assertFalse(ClientRole.isValidRole("agent")); // not one of the three roles
        assertFalse(ClientRole.isValidRole("buyer seller")); // two roles in one token
        assertFalse(ClientRole.isValidRole("(buyer, seller)")); // the display value is not an input
        assertFalse(ClientRole.isValidRole(" buyer")); // trimming is the parser's job
    }

    @Test
    public void fromString_validRole_returnsMatchingConstant() {
        assertEquals(ClientRole.BUYER, ClientRole.fromString("buyer"));
        assertEquals(ClientRole.SELLER, ClientRole.fromString("seller"));
        assertEquals(ClientRole.BOTH, ClientRole.fromString("both"));

        // matching ignores case
        assertEquals(ClientRole.BUYER, ClientRole.fromString("Buyer"));
        assertEquals(ClientRole.BOTH, ClientRole.fromString("BOTH"));
    }

    @Test
    public void fromString_invalidRole_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, ClientRole.MESSAGE_CONSTRAINTS, ()
            -> ClientRole.fromString("agent"));
        assertThrows(IllegalArgumentException.class, ClientRole.MESSAGE_CONSTRAINTS, ()
            -> ClientRole.fromString(""));
        assertThrows(IllegalArgumentException.class, ClientRole.MESSAGE_CONSTRAINTS, ()
            -> ClientRole.fromString("both buyer"));
    }

    @Test
    public void fromString_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ClientRole.fromString(null));
    }

    @Test
    public void getValue_returnsTokenForTheDataFile() {
        assertEquals("buyer", ClientRole.BUYER.getValue());
        assertEquals("seller", ClientRole.SELLER.getValue());
        assertEquals("both", ClientRole.BOTH.getValue());
    }

    @Test
    public void getDisplayValue_returnsTextForTheUser() {
        assertEquals("buyer", ClientRole.BUYER.getDisplayValue());
        assertEquals("seller", ClientRole.SELLER.getDisplayValue());
        assertEquals("(buyer, seller)", ClientRole.BOTH.getDisplayValue());
    }

    @Test
    public void toString_returnsDisplayValue() {
        assertEquals(ClientRole.BOTH.getDisplayValue(), ClientRole.BOTH.toString());
        assertEquals("(buyer, seller)", ClientRole.BOTH.toString());
    }
}
