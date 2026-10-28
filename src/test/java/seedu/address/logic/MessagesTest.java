package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Area;
import seedu.address.model.person.Budget;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.Person;
import seedu.address.model.person.Price;
import seedu.address.testutil.PersonBuilder;

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

    @Test
    public void format_noRequirements_omitsRequirementsSegment() {
        Person person = new PersonBuilder()
                .withName("Req Echo")
                .withPhone("91110003")
                .withEmail("re@example.com")
                .withAddress("1 Road")
                .withTags()
                .build();

        assertEquals("Req Echo; Phone: 91110003; Email: re@example.com; Address: 1 Road; Tags: ",
                Messages.format(person));
    }

    @Test
    public void format_allRequirements_listsEachFieldInDisplayForm() {
        Person person = new PersonBuilder()
                .withName("Req Echo")
                .withPhone("91110003")
                .withEmail("re@example.com")
                .withAddress("1 Road")
                .withTags()
                .withBudget(new Budget(new Price("400000"), new Price("600000")))
                .withAskingPrice(new Price("520000"))
                .withAreas(Set.of(new Area("Tampines"), new Area("Bedok")))
                .withHousingTypes(Set.of(HousingType.HDB, HousingType.CONDO))
                .build();

        assertEquals("Req Echo; Phone: 91110003; Email: re@example.com; Address: 1 Road"
                + "; Requirements: Budget $400,000 to $600,000, Asking $520,000, Bedok, Tampines,"
                + " Condo, HDB; Tags: ", Messages.format(person));
    }

    @Test
    public void format_partialRequirements_omitsAbsentFields() {
        Person budgetOnly = new PersonBuilder()
                .withTags()
                .withBudget(new Budget(new Price("500000"), new Price("500000")))
                .build();
        assertTrue(Messages.format(budgetOnly).contains("; Requirements: Budget $500,000; "));

        Person areasOnly = new PersonBuilder()
                .withTags()
                .withAreas(Set.of(new Area("Orchard")))
                .build();
        assertTrue(Messages.format(areasOnly).contains("; Requirements: Orchard; "));
        assertFalse(Messages.format(areasOnly).contains("Budget"));
        assertFalse(Messages.format(areasOnly).contains("Asking"));
    }
}
