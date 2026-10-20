package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    public void getShowingMessage_noClientsShown_reportsZero() {
        assertEquals("Showing 0 of 34 clients.", Messages.getShowingMessage(0, 34));
    }

    @Test
    public void getShowingMessage_oneClientShown_stillSaysClients() {
        assertEquals("Showing 1 of 34 clients.", Messages.getShowingMessage(1, 34));
    }

    @Test
    public void getShowingMessage_manyClientsShown_reportsBothCounts() {
        assertEquals("Showing 8 of 34 clients.", Messages.getShowingMessage(8, 34));
    }

    @Test
    public void getShowingMessage_totalVaries_reportsTotalIndependently() {
        assertEquals("Showing 1 of 1 clients.", Messages.getShowingMessage(1, 1));
        assertEquals("Showing 0 of 0 clients.", Messages.getShowingMessage(0, 0));
        assertEquals("Showing 2 of 2 clients.", Messages.getShowingMessage(2, 2));
    }
}
