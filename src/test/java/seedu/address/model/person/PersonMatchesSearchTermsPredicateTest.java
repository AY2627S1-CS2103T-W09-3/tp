package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonMatchesSearchTermsPredicateTest {

    @Test
    public void equals() {
        List<String> firstTerms = List.of("first");
        List<String> secondTerms = List.of("first", "second");

        PersonMatchesSearchTermsPredicate firstPredicate = new PersonMatchesSearchTermsPredicate(firstTerms);
        PersonMatchesSearchTermsPredicate secondPredicate = new PersonMatchesSearchTermsPredicate(secondTerms);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        PersonMatchesSearchTermsPredicate firstPredicateCopy = new PersonMatchesSearchTermsPredicate(firstTerms);
        assertTrue(firstPredicate.equals(firstPredicateCopy));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different person -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_nameContainsKeywords_returnsTrue() {
        // One keyword
        PersonMatchesSearchTermsPredicate predicate = new PersonMatchesSearchTermsPredicate(List.of("Alice"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Multiple keywords
        predicate = new PersonMatchesSearchTermsPredicate(List.of("Alice", "Bob"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Only one matching keyword
        predicate = new PersonMatchesSearchTermsPredicate(List.of("Bob", "Carol"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Carol").build()));

        // Mixed-case keywords
        predicate = new PersonMatchesSearchTermsPredicate(List.of("aLIce", "bOB"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));
    }

    @Test
    public void test_phoneOrEmailMatchesKeyword_returnsTrue() {
        // Exact phone
        PersonMatchesSearchTermsPredicate predicate = new PersonMatchesSearchTermsPredicate(List.of("91234567"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice").withPhone("91234567").build()));

        // Email in different case
        predicate = new PersonMatchesSearchTermsPredicate(List.of("ALICE@Email.com"));
        assertTrue(predicate.test(new PersonBuilder().withName("Bob").withEmail("alice@email.com").build()));

        // Name word, with a phone and email that do not match
        predicate = new PersonMatchesSearchTermsPredicate(List.of("Alice"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice").withPhone("91234567")
                .withEmail("bob@email.com").build()));
    }

    @Test
    public void test_nameDoesNotContainKeywords_returnsFalse() {
        // Zero keywords
        PersonMatchesSearchTermsPredicate predicate = new PersonMatchesSearchTermsPredicate(List.of());
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").build()));

        // Non-matching keyword
        predicate = new PersonMatchesSearchTermsPredicate(List.of("Carol"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Keywords match address only
        predicate = new PersonMatchesSearchTermsPredicate(List.of("Main", "Street"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withAddress("Main Street").build()));
    }

    @Test
    public void toStringMethod() {
        List<String> searchTerms = List.of("term1", "term2");
        PersonMatchesSearchTermsPredicate predicate = new PersonMatchesSearchTermsPredicate(searchTerms);

        String expected = PersonMatchesSearchTermsPredicate.class.getCanonicalName()
                + "{searchTerms=" + searchTerms + "}";
        assertEquals(expected, predicate.toString());
    }
}
