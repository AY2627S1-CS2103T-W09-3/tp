package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.ELLE;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonMatchesFilterPredicate;
import seedu.address.model.person.PersonMatchesSearchTermsPredicate;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FilterCommand}.
 */
public class FilterCommandTest {

    private static final Person BUYER_URGENT = new PersonBuilder(ALICE).withRole(ClientRole.BUYER)
            .withTags("urgent").build();
    private static final Person SELLER_URGENT_FRIENDS = new PersonBuilder(BENSON).withRole(ClientRole.SELLER)
            .withTags("urgent", "friends").build();
    private static final Person BOTH_FRIENDS = new PersonBuilder(CARL).withRole(ClientRole.BOTH)
            .withTags("friends").build();
    private static final Person BUYER_NO_TAGS = new PersonBuilder(DANIEL).withRole(ClientRole.BUYER)
            .withTags().build();
    private static final Person NO_ROLE_URGENT = new PersonBuilder(ELLE).withRole(null)
            .withTags("urgent").build();

    private Model model = new ModelManager(getClientBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getClientBook(), new UserPrefs());

    private static AddressBook getClientBook() {
        AddressBook addressBook = new AddressBook();
        for (Person person : List.of(BUYER_URGENT, SELLER_URGENT_FRIENDS, BOTH_FRIENDS, BUYER_NO_TAGS,
                NO_ROLE_URGENT)) {
            addressBook.addPerson(person);
        }
        return addressBook;
    }

    private static PersonMatchesFilterPredicate predicate(ClientRole role, String... tagNames) {
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            tags.add(new Tag(tagName));
        }
        PersonMatchesFilterPredicate.Builder builder = new PersonMatchesFilterPredicate.Builder().withTags(tags);
        if (role != null) {
            builder.withRole(role);
        }
        return builder.build();
    }

    @Test
    public void equals() {
        FilterCommand buyerCommand = new FilterCommand(predicate(ClientRole.BUYER));
        FilterCommand sellerCommand = new FilterCommand(predicate(ClientRole.SELLER));

        // same object -> returns true
        assertTrue(buyerCommand.equals(buyerCommand));

        // same values -> returns true, with the same hashcode
        FilterCommand buyerCommandCopy = new FilterCommand(predicate(ClientRole.BUYER));
        assertTrue(buyerCommand.equals(buyerCommandCopy));
        assertEquals(buyerCommand.hashCode(), buyerCommandCopy.hashCode());

        // different types -> returns false
        assertFalse(buyerCommand.equals(1));

        // null -> returns false
        assertFalse(buyerCommand.equals(null));

        // different predicate -> returns false
        assertFalse(buyerCommand.equals(sellerCommand));
    }

    @Test
    public void execute_buyerRole_showsBuyersAndBoth() {
        PersonMatchesFilterPredicate predicate = predicate(ClientRole.BUYER);
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FilterCommand(predicate), model, "Showing 3 of 5 clients.", expectedModel);
        assertEquals(List.of(BUYER_URGENT, BOTH_FRIENDS, BUYER_NO_TAGS), model.getFilteredPersonList());
    }

    @Test
    public void execute_sellerRole_showsSellersAndBoth() {
        PersonMatchesFilterPredicate predicate = predicate(ClientRole.SELLER);
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FilterCommand(predicate), model, "Showing 2 of 5 clients.", expectedModel);
        assertEquals(List.of(SELLER_URGENT_FRIENDS, BOTH_FRIENDS), model.getFilteredPersonList());
    }

    @Test
    public void execute_bothRole_showsOnlyBoth() {
        PersonMatchesFilterPredicate predicate = predicate(ClientRole.BOTH);
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FilterCommand(predicate), model, "Showing 1 of 5 clients.", expectedModel);
        assertEquals(List.of(BOTH_FRIENDS), model.getFilteredPersonList());
    }

    @Test
    public void execute_tagOnly_showsMatchingClientsIgnoringCase() {
        PersonMatchesFilterPredicate predicate = predicate(null, "URGENT");
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FilterCommand(predicate), model, "Showing 3 of 5 clients.", expectedModel);
        assertEquals(List.of(BUYER_URGENT, SELLER_URGENT_FRIENDS, NO_ROLE_URGENT), model.getFilteredPersonList());
    }

    @Test
    public void execute_repeatedTags_combineWithOr() {
        PersonMatchesFilterPredicate predicate = predicate(null, "urgent", "friends");
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FilterCommand(predicate), model, "Showing 4 of 5 clients.", expectedModel);
    }

    @Test
    public void execute_roleAndTag_combineWithAnd() {
        PersonMatchesFilterPredicate predicate = predicate(ClientRole.BUYER, "urgent");
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FilterCommand(predicate), model, "Showing 1 of 5 clients.", expectedModel);
        assertEquals(List.of(BUYER_URGENT), model.getFilteredPersonList());
    }

    @Test
    public void execute_noMatch_showsZeroOfTotal() {
        PersonMatchesFilterPredicate predicate = predicate(ClientRole.SELLER, "weekend");
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FilterCommand(predicate), model, "Showing 0 of 5 clients.", expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_noCriteria_showsEveryone() {
        model.updateFilteredPersonList(predicate(ClientRole.BOTH));
        PersonMatchesFilterPredicate predicate = predicate(null);

        assertCommandSuccess(new FilterCommand(predicate), model, "Showing 5 of 5 clients.", expectedModel);
        assertEquals(5, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_secondFilter_replacesTheFirst() {
        new FilterCommand(predicate(ClientRole.BUYER)).execute(model);

        // Stacking would show only the urgent buyer. Replacing shows every urgent client.
        PersonMatchesFilterPredicate predicate = predicate(null, "urgent");
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FilterCommand(predicate), model, "Showing 3 of 5 clients.", expectedModel);
    }

    @Test
    public void execute_findAfterFilter_replacesTheFilter() {
        new FilterCommand(predicate(ClientRole.SELLER)).execute(model);

        // Alice is a buyer, so she is visible only if find replaced the seller filter.
        PersonMatchesSearchTermsPredicate findPredicate = new PersonMatchesSearchTermsPredicate(List.of("Alice"));
        expectedModel.updateFilteredPersonList(findPredicate);

        new FindCommand(findPredicate).execute(model);

        assertEquals(expectedModel.getFilteredPersonList(), model.getFilteredPersonList());
        assertEquals(List.of(BUYER_URGENT), model.getFilteredPersonList());
    }

    @Test
    public void execute_filterAfterFind_replacesTheSearch() {
        new FindCommand(new PersonMatchesSearchTermsPredicate(List.of("Daniel"))).execute(model);

        // Daniel is a buyer, so intersecting would leave nobody. Replacing shows the two sellers.
        PersonMatchesFilterPredicate predicate = predicate(ClientRole.SELLER);
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new FilterCommand(predicate), model, "Showing 2 of 5 clients.", expectedModel);
    }

    @Test
    public void execute_listAfterFilter_showsEveryone() {
        new FilterCommand(predicate(ClientRole.BOTH, "friends")).execute(model);

        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
        assertEquals(5, model.getFilteredPersonList().size());
    }

    @Test
    public void toStringMethod() {
        PersonMatchesFilterPredicate predicate = predicate(ClientRole.BUYER);
        FilterCommand filterCommand = new FilterCommand(predicate);
        String expected = FilterCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, filterCommand.toString());
    }
}
