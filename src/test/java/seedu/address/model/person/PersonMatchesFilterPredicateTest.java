package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class PersonMatchesFilterPredicateTest {

    private static Set<Tag> tagSet(String... tagNames) {
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            tags.add(new Tag(tagName));
        }
        return tags;
    }

    private static PersonMatchesFilterPredicate roleOnly(ClientRole role) {
        return new PersonMatchesFilterPredicate(Optional.of(role), Set.of());
    }

    private static PersonMatchesFilterPredicate tagsOnly(String... tagNames) {
        return new PersonMatchesFilterPredicate(Optional.empty(), tagSet(tagNames));
    }

    private static PersonMatchesFilterPredicate roleAndTags(ClientRole role, String... tagNames) {
        return new PersonMatchesFilterPredicate(Optional.of(role), tagSet(tagNames));
    }

    @Test
    public void equals() {
        PersonMatchesFilterPredicate predicate = roleAndTags(ClientRole.BUYER, "urgent");

        // same object -> returns true
        assertTrue(predicate.equals(predicate));

        // same values -> returns true, with the same hashcode
        PersonMatchesFilterPredicate copy = roleAndTags(ClientRole.BUYER, "urgent");
        assertTrue(predicate.equals(copy));
        assertEquals(predicate.hashCode(), copy.hashCode());

        // null -> returns false
        assertFalse(predicate.equals(null));

        // different types -> returns false
        assertFalse(predicate.equals(5));

        // different role -> returns false
        assertFalse(predicate.equals(roleAndTags(ClientRole.SELLER, "urgent")));
        assertNotEquals(predicate.hashCode(), roleAndTags(ClientRole.SELLER, "urgent").hashCode());

        // different tags -> returns false
        assertFalse(predicate.equals(roleAndTags(ClientRole.BUYER, "weekend")));

        // role missing -> returns false
        assertFalse(predicate.equals(tagsOnly("urgent")));
    }

    @Test
    public void test_buyerRole_matchesBuyersAndBoth() {
        PersonMatchesFilterPredicate predicate = roleOnly(ClientRole.BUYER);

        assertTrue(predicate.test(new PersonBuilder().withRole(ClientRole.BUYER).build()));
        assertTrue(predicate.test(new PersonBuilder().withRole(ClientRole.BOTH).build()));
        assertFalse(predicate.test(new PersonBuilder().withRole(ClientRole.SELLER).build()));
    }

    @Test
    public void test_sellerRole_matchesSellersAndBoth() {
        PersonMatchesFilterPredicate predicate = roleOnly(ClientRole.SELLER);

        assertTrue(predicate.test(new PersonBuilder().withRole(ClientRole.SELLER).build()));
        assertTrue(predicate.test(new PersonBuilder().withRole(ClientRole.BOTH).build()));
        assertFalse(predicate.test(new PersonBuilder().withRole(ClientRole.BUYER).build()));
    }

    @Test
    public void test_bothRole_matchesOnlyBoth() {
        PersonMatchesFilterPredicate predicate = roleOnly(ClientRole.BOTH);

        assertTrue(predicate.test(new PersonBuilder().withRole(ClientRole.BOTH).build()));
        assertFalse(predicate.test(new PersonBuilder().withRole(ClientRole.BUYER).build()));
        assertFalse(predicate.test(new PersonBuilder().withRole(ClientRole.SELLER).build()));
    }

    @Test
    public void test_clientWithNoRole_neverMatchesARole() {
        Person noRole = new PersonBuilder().withRole(null).build();

        assertFalse(roleOnly(ClientRole.BUYER).test(noRole));
        assertFalse(roleOnly(ClientRole.SELLER).test(noRole));
        assertFalse(roleOnly(ClientRole.BOTH).test(noRole));
    }

    @Test
    public void test_tags_matchIgnoringCaseWithOr() {
        Person person = new PersonBuilder().withTags("urgent", "firsttime").build();

        // one tag
        assertTrue(tagsOnly("urgent").test(person));

        // mixed case
        assertTrue(tagsOnly("URGENT").test(person));
        assertTrue(tagsOnly("FirstTime").test(person));

        // any one of several tags
        assertTrue(tagsOnly("weekend", "urgent").test(person));

        // no matching tag
        assertFalse(tagsOnly("weekend", "upgrading").test(person));

        // partial tag name
        assertFalse(tagsOnly("urg").test(person));

        // client with no tags
        assertFalse(tagsOnly("urgent").test(new PersonBuilder().withTags().build()));
    }

    @Test
    public void test_roleAndTags_bothMustMatch() {
        PersonMatchesFilterPredicate predicate = roleAndTags(ClientRole.BUYER, "urgent");

        // role and tag both match
        assertTrue(predicate.test(new PersonBuilder().withRole(ClientRole.BUYER).withTags("urgent").build()));

        // only the role matches
        assertFalse(predicate.test(new PersonBuilder().withRole(ClientRole.BUYER).withTags("weekend").build()));

        // only the tag matches
        assertFalse(predicate.test(new PersonBuilder().withRole(ClientRole.SELLER).withTags("urgent").build()));
    }

    @Test
    public void test_noCriteria_matchesEveryone() {
        PersonMatchesFilterPredicate predicate = new PersonMatchesFilterPredicate(Optional.empty(), Set.of());

        assertTrue(predicate.test(new PersonBuilder().withRole(null).withTags().build()));
        assertTrue(predicate.test(new PersonBuilder().withRole(ClientRole.BOTH).withTags("urgent").build()));
    }

    @Test
    public void toStringMethod() {
        Set<Tag> tags = Set.of(new Tag("urgent"));
        PersonMatchesFilterPredicate predicate = new PersonMatchesFilterPredicate(Optional.of(ClientRole.BUYER), tags);

        String expected = PersonMatchesFilterPredicate.class.getCanonicalName()
                + "{role=" + Optional.of(ClientRole.BUYER) + ", tags=" + tags + "}";
        assertEquals(expected, predicate.toString());
    }
}
