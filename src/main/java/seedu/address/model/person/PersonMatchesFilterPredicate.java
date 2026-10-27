package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Tests that a {@code Person} matches the role and the tags given.
 * A role of buyer matches buyers and clients who are both, a role of seller matches sellers and clients who are
 * both, and a role of both matches only clients who are both. A client with no role never matches a role.
 * A tag matches if the person has a tag with the same name, ignoring case, and a person matches the tags
 * if any one of them matches. A role and tags must both match, and a missing role or no tags always match.
 */
public class PersonMatchesFilterPredicate implements Predicate<Person> {
    private final Optional<ClientRole> role;
    private final Set<Tag> tags;

    /**
     * Creates a predicate that matches the given {@code role}, if present, and any of the given {@code tags},
     * if there are any.
     */
    public PersonMatchesFilterPredicate(Optional<ClientRole> role, Set<Tag> tags) {
        this.role = requireNonNull(role);
        this.tags = requireNonNull(tags);
    }

    @Override
    public boolean test(Person person) {
        return matchesRole(person) && matchesTags(person);
    }

    private boolean matchesRole(Person person) {
        if (role.isEmpty()) {
            return true;
        }

        ClientRole personRole = person.getRole();
        if (personRole == null) {
            return false;
        }

        return switch (role.get()) {
            case BUYER -> personRole == ClientRole.BUYER || personRole == ClientRole.BOTH;
            case SELLER -> personRole == ClientRole.SELLER || personRole == ClientRole.BOTH;
            case BOTH -> personRole == ClientRole.BOTH;
        };
    }

    private boolean matchesTags(Person person) {
        if (tags.isEmpty()) {
            return true;
        }

        return person.getTags().stream().anyMatch(tags::contains);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof PersonMatchesFilterPredicate otherPredicate)) {
            return false;
        }

        return role.equals(otherPredicate.role) && tags.equals(otherPredicate.tags);
    }

    @Override
    public int hashCode() {
        return 31 * role.hashCode() + tags.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("role", role)
                .add("tags", tags)
                .toString();
    }
}
