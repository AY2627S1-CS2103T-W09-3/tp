package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MessagesTest {

    @Test
    public void duplicatePhoneMessage_mentionsCoBuyerWorkaround() {
        assertTrue(Messages.MESSAGE_DUPLICATE_PHONE.contains("t/couple"));
    }

    @Test
    public void duplicateEmailMessage_mentionsCoBuyerWorkaround() {
        assertTrue(Messages.MESSAGE_DUPLICATE_EMAIL.contains("t/couple"));
    }
}
