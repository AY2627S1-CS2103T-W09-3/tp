package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.ClientRole;

public class PersonCardTest {

    @Test
    public void getRoleDisplayText_eachRole_returnsBadgeText() {
        assertEquals("Buyer", PersonCard.getRoleDisplayText(ClientRole.BUYER));
        assertEquals("Seller", PersonCard.getRoleDisplayText(ClientRole.SELLER));
        assertEquals("Buyer, Seller", PersonCard.getRoleDisplayText(ClientRole.BOTH));
    }

    @Test
    public void getRoleStyleClass_eachRole_returnsColourClass() {
        assertEquals("role-buyer", PersonCard.getRoleStyleClass(ClientRole.BUYER));
        assertEquals("role-seller", PersonCard.getRoleStyleClass(ClientRole.SELLER));
        assertEquals("role-both", PersonCard.getRoleStyleClass(ClientRole.BOTH));
    }
}
