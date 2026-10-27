package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Tests that a {@code Person} matches the role, the tags, the areas and the housing types given.
 * A role of buyer matches buyers and clients who are both, a role of seller matches sellers and clients who are
 * both, and a role of both matches only clients who are both. A client with no role never matches a role.
 * A tag matches if the person has a tag with the same name, ignoring case, and a person matches the tags
 * if any one of them matches. A person matches the areas if one of their preferred areas is any of the areas
 * given, and a client with no preferred areas never matches. A person matches the housing types in the same way,
 * using their preferred housing types. Every criterion that is given must match, and a missing role, no tags, no
 * areas or no housing types always match.
 */
public class PersonMatchesFilterPredicate implements Predicate<Person> {
    private final Optional<ClientRole> role;
    private final Set<Tag> tags;
    private final Set<Area> areas;
    private final Set<HousingType> housingTypes;

    private PersonMatchesFilterPredicate(Optional<ClientRole> role, Set<Tag> tags, Set<Area> areas,
            Set<HousingType> housingTypes) {
        this.role = requireNonNull(role);
        this.tags = requireNonNull(tags);
        this.areas = requireNonNull(areas);
        this.housingTypes = requireNonNull(housingTypes);
    }

    @Override
    public boolean test(Person person) {
        return matchesRole(person) && matchesTags(person) && matchesAreas(person) && matchesHousingTypes(person);
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

        return person.getTags().stream().anyMatch(personTag ->
                tags.stream().anyMatch(tag -> tag.tagName.equalsIgnoreCase(personTag.tagName)));
    }

    private boolean matchesAreas(Person person) {
        if (areas.isEmpty()) {
            return true;
        }

        return person.getAreas().stream().anyMatch(areas::contains);
    }

    private boolean matchesHousingTypes(Person person) {
        if (housingTypes.isEmpty()) {
            return true;
        }

        return person.getHousingTypes().stream().anyMatch(housingTypes::contains);
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

        return role.equals(otherPredicate.role) && tags.equals(otherPredicate.tags)
                && areas.equals(otherPredicate.areas) && housingTypes.equals(otherPredicate.housingTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, tags, areas, housingTypes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("role", role)
                .add("tags", tags)
                .add("areas", areas)
                .add("housingTypes", housingTypes)
                .toString();
    }

    /**
     * Builds a {@code PersonMatchesFilterPredicate}. A criterion that is not set always matches.
     */
    public static class Builder {
        private Optional<ClientRole> role = Optional.empty();
        private Set<Tag> tags = new HashSet<>();
        private Set<Area> areas = new HashSet<>();
        private Set<HousingType> housingTypes = new HashSet<>();

        /**
         * Requires a client to have the given {@code role}, or to be both when the role is buyer or seller.
         */
        public Builder withRole(ClientRole role) {
            this.role = Optional.of(requireNonNull(role));
            return this;
        }

        /**
         * Requires a client to have any one of the given {@code tags}, if there are any.
         */
        public Builder withTags(Set<Tag> tags) {
            this.tags = new HashSet<>(requireNonNull(tags));
            return this;
        }

        /**
         * Requires a client to have any one of the given {@code areas} as a preferred area, if there are any.
         */
        public Builder withAreas(Set<Area> areas) {
            this.areas = new HashSet<>(requireNonNull(areas));
            return this;
        }

        /**
         * Requires a client to have any one of the given {@code housingTypes} as a preferred housing type, if there
         * are any.
         */
        public Builder withHousingTypes(Set<HousingType> housingTypes) {
            this.housingTypes = new HashSet<>(requireNonNull(housingTypes));
            return this;
        }

        /**
         * Returns a predicate that matches the criteria set so far.
         */
        public PersonMatchesFilterPredicate build() {
            return new PersonMatchesFilterPredicate(role, tags, areas, housingTypes);
        }
    }
}
