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
    public void test_partialName_returnsTrue() {
        // A term inside Tan, Tania and Anand
        PersonMatchesSearchTermsPredicate predicate = new PersonMatchesSearchTermsPredicate(List.of("an"));
        assertTrue(predicate.test(personWithName("Tan")));
        assertTrue(predicate.test(personWithName("Tania")));
        assertTrue(predicate.test(personWithName("Anand")));

        // Start, middle and end of a word
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("Ali")).test(personWithName("Alice Bob")));
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("lic")).test(personWithName("Alice Bob")));
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("ice")).test(personWithName("Alice Bob")));

        // Inside the second word
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("ob")).test(personWithName("Alice Bob")));

        // Mixed case
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("aLI")).test(personWithName("Alice Bob")));
    }

    @Test
    public void test_partialPhone_returnsTrue() {
        // Start, middle and end of the number
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("912")).test(personWithPhone("91234567")));
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("345")).test(personWithPhone("91234567")));
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("567")).test(personWithPhone("91234567")));

        // The whole number still matches
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("91234567")).test(personWithPhone("91234567")));
    }

    @Test
    public void test_partialEmail_returnsTrue() {
        // Inside the domain
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("gmail")).test(personWithEmail("alice@gmail.com")));

        // Start, middle and end of the address
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("alice@")).test(personWithEmail("alice@gmail.com")));
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("@gmail")).test(personWithEmail("alice@gmail.com")));
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of(".com")).test(personWithEmail("alice@gmail.com")));

        // Mixed case
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("GMail")).test(personWithEmail("alice@gmail.com")));
    }

    @Test
    public void test_oneCharacterTerm_matchesEveryFieldContainingIt() {
        // Name only, phone only and email only
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("z")).test(personWithName("Zed")));
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("9")).test(personWithPhone("81119111")));
        assertTrue(new PersonMatchesSearchTermsPredicate(List.of("@")).test(personWithEmail("alice@gmail.com")));

        // None of the three fields contain it
        assertFalse(new PersonMatchesSearchTermsPredicate(List.of("z"))
                .test(new PersonBuilder().withName("Bob").withPhone("81111111").withEmail("bob@mail.com").build()));
    }

    @Test
    public void test_multipleTermsWithOnePartialMatch_returnsTrue() {
        PersonMatchesSearchTermsPredicate predicate = new PersonMatchesSearchTermsPredicate(List.of("zzz", "912"));
        assertTrue(predicate.test(personWithPhone("91234567")));

        // No term matches any field
        predicate = new PersonMatchesSearchTermsPredicate(List.of("zzz", "qqq"));
        assertFalse(predicate.test(personWithPhone("91234567")));
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

    private static Person personWithName(String name) {
        return new PersonBuilder().withName(name).withPhone("81111111").withEmail("p@example.com").build();
    }

    private static Person personWithPhone(String phone) {
        return new PersonBuilder().withName("Bob").withPhone(phone).withEmail("p@example.com").build();
    }

    private static Person personWithEmail(String email) {
        return new PersonBuilder().withName("Bob").withPhone("81111111").withEmail(email).build();
    }
}
