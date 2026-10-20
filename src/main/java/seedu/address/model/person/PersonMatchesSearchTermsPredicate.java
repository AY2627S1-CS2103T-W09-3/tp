package seedu.address.model.person;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Name}, {@code Phone} or {@code Email} matches any of the search terms given.
 * A field matches if it contains a term anywhere, ignoring case.
 */
public class PersonMatchesSearchTermsPredicate implements Predicate<Person> {
    private final List<String> searchTerms;

    public PersonMatchesSearchTermsPredicate(List<String> searchTerms) {
        this.searchTerms = searchTerms;
    }

    @Override
    public boolean test(Person person) {
        return searchTerms.stream().anyMatch(term ->
                containsIgnoreCase(person.getName().fullName, term)
                        || containsIgnoreCase(person.getPhone().value, term)
                        || containsIgnoreCase(person.getEmail().value, term));
    }

    private static boolean containsIgnoreCase(String field, String term) {
        return field.toLowerCase(Locale.ROOT).contains(term.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof PersonMatchesSearchTermsPredicate otherPersonMatchesSearchTermsPredicate)) {
            return false;
        }

        return searchTerms.equals(otherPersonMatchesSearchTermsPredicate.searchTerms);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("searchTerms", searchTerms).toString();
    }
}
