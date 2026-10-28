package seedu.address.model.person;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Name}, {@code Phone}, {@code Email} or {@code Address} matches all of the
 * search terms given.
 * A term matches if any one of those fields contains it anywhere, ignoring case and equivalent Unicode spellings,
 * so the terms can be matched by different fields. No search terms never match.
 */
public class PersonMatchesSearchTermsPredicate implements Predicate<Person> {
    private final List<String> searchTerms;

    public PersonMatchesSearchTermsPredicate(List<String> searchTerms) {
        this.searchTerms = searchTerms;
    }

    @Override
    public boolean test(Person person) {
        return !searchTerms.isEmpty() && searchTerms.stream().allMatch(term ->
                containsIgnoreCase(person.getName().fullName, term)
                        || containsIgnoreCase(person.getPhone().value, term)
                        || containsIgnoreCase(person.getEmail().value, term)
                        || containsIgnoreCase(person.getAddress().value, term));
    }

    private static boolean containsIgnoreCase(String field, String term) {
        return Normalizer.normalize(field, Normalizer.Form.NFC).toLowerCase(Locale.ROOT)
                .contains(Normalizer.normalize(term, Normalizer.Form.NFC).toLowerCase(Locale.ROOT));
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
