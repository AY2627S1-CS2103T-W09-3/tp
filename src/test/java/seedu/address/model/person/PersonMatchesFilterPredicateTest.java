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

    private static Set<Area> areaSet(String... areaNames) {
        Set<Area> areas = new HashSet<>();
        for (String areaName : areaNames) {
            areas.add(new Area(areaName));
        }
        return areas;
    }

    private static PersonMatchesFilterPredicate areasOnly(String... areaNames) {
        return new PersonMatchesFilterPredicate.Builder().withAreas(areaSet(areaNames)).build();
    }

    private static Set<HousingType> housingTypeSet(HousingType... housingTypes) {
        return new HashSet<>(Set.of(housingTypes));
    }

    private static PersonMatchesFilterPredicate housingTypesOnly(HousingType... housingTypes) {
        return new PersonMatchesFilterPredicate.Builder().withHousingTypes(housingTypeSet(housingTypes)).build();
    }

    private static PriceRange priceRange(String min, String max) {
        return new PriceRange(new Price(min), new Price(max));
    }

    private static Budget budget(String min, String max) {
        return new Budget(new Price(min), new Price(max));
    }

    private static PersonMatchesFilterPredicate budgetOnly(PriceRange priceRange) {
        return new PersonMatchesFilterPredicate.Builder().withBudget(priceRange).build();
    }

    private static PersonMatchesFilterPredicate askingPriceOnly(PriceRange priceRange) {
        return new PersonMatchesFilterPredicate.Builder().withAskingPrice(priceRange).build();
    }

    private static PersonMatchesFilterPredicate roleOnly(ClientRole role) {
        return new PersonMatchesFilterPredicate.Builder().withRole(role).build();
    }

    private static PersonMatchesFilterPredicate tagsOnly(String... tagNames) {
        return new PersonMatchesFilterPredicate.Builder().withTags(tagSet(tagNames)).build();
    }

    private static PersonMatchesFilterPredicate roleAndTags(ClientRole role, String... tagNames) {
        return new PersonMatchesFilterPredicate.Builder().withRole(role).withTags(tagSet(tagNames)).build();
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

        // different areas -> returns false
        assertTrue(areasOnly("Bedok").equals(areasOnly("Bedok")));
        assertFalse(areasOnly("Bedok").equals(areasOnly("Bishan")));
        assertFalse(areasOnly("Bedok").equals(roleOnly(ClientRole.BUYER)));

        // different housing types -> returns false
        assertTrue(housingTypesOnly(HousingType.HDB).equals(housingTypesOnly(HousingType.HDB)));
        assertFalse(housingTypesOnly(HousingType.HDB).equals(housingTypesOnly(HousingType.EC)));
        assertFalse(housingTypesOnly(HousingType.HDB).equals(areasOnly("Bedok")));

        // different budget -> returns false
        assertTrue(budgetOnly(priceRange("1000", "2000")).equals(budgetOnly(priceRange("1000", "2000"))));
        assertFalse(budgetOnly(priceRange("1000", "2000")).equals(budgetOnly(priceRange("1000", "3000"))));
        assertFalse(budgetOnly(priceRange("1000", "2000")).equals(housingTypesOnly(HousingType.HDB)));

        // different asking price -> returns false
        assertTrue(askingPriceOnly(priceRange("1000", "2000")).equals(askingPriceOnly(priceRange("1000", "2000"))));
        assertFalse(askingPriceOnly(priceRange("1000", "2000")).equals(askingPriceOnly(priceRange("1000", "3000"))));

        // the same range as a budget and as an asking price -> returns false
        assertFalse(askingPriceOnly(priceRange("1000", "2000")).equals(budgetOnly(priceRange("1000", "2000"))));
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
    public void test_areas_matchAnyPreferredAreaIgnoringCase() {
        Person person = new PersonBuilder().withAreas(areaSet("Bishan", "Toa Payoh")).build();

        // one area
        assertTrue(areasOnly("Bishan").test(person));

        // mixed case
        assertTrue(areasOnly("bishan").test(person));
        assertTrue(areasOnly("TOA PAYOH").test(person));

        // any one of several areas
        assertTrue(areasOnly("Orchard", "Bishan").test(person));

        // no matching area
        assertFalse(areasOnly("Orchard", "Tampines").test(person));
    }

    @Test
    public void test_clientWithNoAreas_neverMatchesAnArea() {
        Person noAreas = new PersonBuilder().withAreas(Set.of()).build();

        assertFalse(areasOnly("Bishan").test(noAreas));
        assertFalse(areasOnly("Bishan", "Orchard").test(noAreas));
    }

    @Test
    public void test_roleTagsAndAreas_allMustMatch() {
        PersonMatchesFilterPredicate predicate = new PersonMatchesFilterPredicate.Builder()
                .withRole(ClientRole.BUYER).withTags(tagSet("urgent")).withAreas(areaSet("Bishan")).build();

        // role, tag and area all match
        assertTrue(predicate.test(new PersonBuilder().withRole(ClientRole.BUYER).withTags("urgent")
                .withAreas(areaSet("Bishan")).build()));

        // only the role and the tag match
        assertFalse(predicate.test(new PersonBuilder().withRole(ClientRole.BUYER).withTags("urgent")
                .withAreas(areaSet("Orchard")).build()));

        // only the role and the area match
        assertFalse(predicate.test(new PersonBuilder().withRole(ClientRole.BUYER).withTags("weekend")
                .withAreas(areaSet("Bishan")).build()));

        // only the tag and the area match
        assertFalse(predicate.test(new PersonBuilder().withRole(ClientRole.SELLER).withTags("urgent")
                .withAreas(areaSet("Bishan")).build()));
    }

    @Test
    public void test_housingTypes_matchAnyPreferredHousingType() {
        Person person = new PersonBuilder().withHousingTypes(housingTypeSet(HousingType.HDB, HousingType.CONDO))
                .build();

        // one housing type
        assertTrue(housingTypesOnly(HousingType.HDB).test(person));
        assertTrue(housingTypesOnly(HousingType.CONDO).test(person));

        // any one of several housing types
        assertTrue(housingTypesOnly(HousingType.LANDED, HousingType.HDB).test(person));

        // no matching housing type
        assertFalse(housingTypesOnly(HousingType.EC).test(person));
        assertFalse(housingTypesOnly(HousingType.EC, HousingType.LANDED).test(person));
    }

    @Test
    public void test_clientWithNoHousingTypes_neverMatchesAHousingType() {
        Person noHousingTypes = new PersonBuilder().withHousingTypes(Set.of()).build();

        assertFalse(housingTypesOnly(HousingType.HDB).test(noHousingTypes));
        assertFalse(housingTypesOnly(HousingType.HDB, HousingType.EC).test(noHousingTypes));
    }

    @Test
    public void test_areasAndHousingTypes_bothMustMatch() {
        PersonMatchesFilterPredicate predicate = new PersonMatchesFilterPredicate.Builder()
                .withAreas(areaSet("Bishan")).withHousingTypes(housingTypeSet(HousingType.HDB)).build();

        // area and housing type both match
        assertTrue(predicate.test(new PersonBuilder().withAreas(areaSet("Bishan"))
                .withHousingTypes(housingTypeSet(HousingType.HDB, HousingType.CONDO)).build()));

        // only the area matches
        assertFalse(predicate.test(new PersonBuilder().withAreas(areaSet("Bishan"))
                .withHousingTypes(housingTypeSet(HousingType.EC)).build()));

        // only the housing type matches
        assertFalse(predicate.test(new PersonBuilder().withAreas(areaSet("Orchard"))
                .withHousingTypes(housingTypeSet(HousingType.HDB)).build()));
    }

    @Test
    public void test_budget_matchesBudgetsThatOverlapTheRange() {
        Person person = new PersonBuilder().withBudget(budget("800000", "1200000")).build();

        // range inside, around, and crossing either end of the client's budget
        assertTrue(budgetOnly(priceRange("900000", "1000000")).test(person));
        assertTrue(budgetOnly(priceRange("500000", "2000000")).test(person));
        assertTrue(budgetOnly(priceRange("500000", "900000")).test(person));
        assertTrue(budgetOnly(priceRange("1100000", "2000000")).test(person));

        // range that only touches an end of the client's budget
        assertTrue(budgetOnly(priceRange("500000", "800000")).test(person));
        assertTrue(budgetOnly(priceRange("1200000", "2000000")).test(person));

        // range that shares no price with the client's budget
        assertFalse(budgetOnly(priceRange("500000", "799999")).test(person));
        assertFalse(budgetOnly(priceRange("1200001", "2000000")).test(person));
    }

    @Test
    public void test_singlePriceBudget_matchesBudgetsThatIncludeThePrice() {
        Person person = new PersonBuilder().withBudget(budget("800000", "1200000")).build();

        assertTrue(budgetOnly(new PriceRange(new Price("1000000"))).test(person));
        assertTrue(budgetOnly(new PriceRange(new Price("800000"))).test(person));
        assertTrue(budgetOnly(new PriceRange(new Price("1200000"))).test(person));
        assertFalse(budgetOnly(new PriceRange(new Price("799999"))).test(person));
        assertFalse(budgetOnly(new PriceRange(new Price("1200001"))).test(person));
    }

    @Test
    public void test_clientWithNoBudget_neverMatchesABudget() {
        Person noBudget = new PersonBuilder().withBudget(null).withAskingPrice(new Price("900000")).build();

        assertFalse(budgetOnly(priceRange("1", "1000000000")).test(noBudget));
        assertFalse(budgetOnly(new PriceRange(new Price("900000"))).test(noBudget));
    }

    @Test
    public void test_roleAndBudget_bothMustMatch() {
        PersonMatchesFilterPredicate predicate = new PersonMatchesFilterPredicate.Builder()
                .withRole(ClientRole.BUYER).withBudget(priceRange("900000", "1000000")).build();

        // role and budget both match
        assertTrue(predicate.test(new PersonBuilder().withRole(ClientRole.BUYER)
                .withBudget(budget("800000", "1200000")).build()));

        // only the role matches
        assertFalse(predicate.test(new PersonBuilder().withRole(ClientRole.BUYER)
                .withBudget(budget("100000", "200000")).build()));

        // only the budget matches
        assertFalse(predicate.test(new PersonBuilder().withRole(ClientRole.SELLER)
                .withBudget(budget("800000", "1200000")).build()));
    }

    @Test
    public void test_askingPrice_matchesPricesWithinTheRange() {
        Person person = new PersonBuilder().withAskingPrice(new Price("950000")).build();

        // range around the asking price, and ranges that only touch it
        assertTrue(askingPriceOnly(priceRange("900000", "1000000")).test(person));
        assertTrue(askingPriceOnly(priceRange("950000", "1000000")).test(person));
        assertTrue(askingPriceOnly(priceRange("900000", "950000")).test(person));

        // range that does not include the asking price
        assertFalse(askingPriceOnly(priceRange("100000", "949999")).test(person));
        assertFalse(askingPriceOnly(priceRange("950001", "1000000")).test(person));
    }

    @Test
    public void test_singleAskingPrice_matchesOnlyThatPrice() {
        Person person = new PersonBuilder().withAskingPrice(new Price("950000")).build();

        assertTrue(askingPriceOnly(new PriceRange(new Price("950000"))).test(person));
        assertFalse(askingPriceOnly(new PriceRange(new Price("949999"))).test(person));
        assertFalse(askingPriceOnly(new PriceRange(new Price("950001"))).test(person));
    }

    @Test
    public void test_clientWithNoAskingPrice_neverMatchesAnAskingPrice() {
        Person noAskingPrice = new PersonBuilder().withAskingPrice(null).withBudget(budget("800000", "1200000"))
                .build();

        assertFalse(askingPriceOnly(priceRange("1", "1000000000")).test(noAskingPrice));
        assertFalse(askingPriceOnly(new PriceRange(new Price("900000"))).test(noAskingPrice));
    }

    @Test
    public void test_budgetAndAskingPrice_bothMustMatch() {
        PersonMatchesFilterPredicate predicate = new PersonMatchesFilterPredicate.Builder()
                .withBudget(new PriceRange(new Price("800000")))
                .withAskingPrice(priceRange("800000", "900000")).build();

        // budget and asking price both match
        assertTrue(predicate.test(new PersonBuilder().withBudget(budget("700000", "900000"))
                .withAskingPrice(new Price("850000")).build()));

        // only the budget matches
        assertFalse(predicate.test(new PersonBuilder().withBudget(budget("700000", "900000"))
                .withAskingPrice(new Price("950000")).build()));

        // only the asking price matches
        assertFalse(predicate.test(new PersonBuilder().withBudget(budget("100000", "200000"))
                .withAskingPrice(new Price("850000")).build()));
    }

    @Test
    public void test_noCriteria_matchesEveryone() {
        PersonMatchesFilterPredicate predicate = new PersonMatchesFilterPredicate.Builder().build();

        assertTrue(predicate.test(new PersonBuilder().withRole(null).withTags().build()));
        assertTrue(predicate.test(new PersonBuilder().withRole(ClientRole.BOTH).withTags("urgent").build()));
    }

    @Test
    public void toStringMethod() {
        Set<Tag> tags = Set.of(new Tag("urgent"));
        PersonMatchesFilterPredicate predicate = new PersonMatchesFilterPredicate.Builder()
                .withRole(ClientRole.BUYER).withTags(tags).build();

        String expected = PersonMatchesFilterPredicate.class.getCanonicalName()
                + "{role=" + Optional.of(ClientRole.BUYER) + ", tags=" + tags + ", areas=" + Set.of()
                + ", housingTypes=" + Set.of() + ", budget=" + Optional.empty()
                + ", askingPrice=" + Optional.empty() + "}";
        assertEquals(expected, predicate.toString());
    }
}
