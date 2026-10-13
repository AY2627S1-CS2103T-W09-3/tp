package seedu.address.model.person;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Name}, {@code Phone} or {@code Email} matches any of the search terms given.
 * A name matches if a term equals one of its words, ignoring case. A phone matches if a term equals it exactly.
 * An email matches if a term equals it, ignoring case.
 */
public class PersonMatchesSearchTermsPredicate implements Predicate<Person> {
    private final List<String> searchTerms;

    public PersonMatchesSearchTermsPredicate(List<String> searchTerms) {
        this.searchTerms = searchTerms;
    }

    @Override
    public boolean test(Person person) {
        return searchTerms.stream().anyMatch(term ->
                StringUtil.containsWordIgnoreCase(person.getName().fullName, term)
                        || person.getPhone().value.equals(term)
                        || person.getEmail().value.equalsIgnoreCase(term));
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
