package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PriceRangeTest {

    private static PriceRange range(String min, String max) {
        return new PriceRange(new Price(min), new Price(max));
    }

    private static Budget budget(String min, String max) {
        return new Budget(new Price(min), new Price(max));
    }

    @Test
    public void constructor_nullPrice_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PriceRange(null, new Price("1000")));
        assertThrows(NullPointerException.class, () -> new PriceRange(new Price("1000"), null));
        assertThrows(NullPointerException.class, () -> new PriceRange(null));
    }

    @Test
    public void constructor_minimumAboveMaximum_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                range("2000", "1000"));
        assertEquals(PriceRange.MESSAGE_CONSTRAINTS, exception.getMessage());
    }

    @Test
    public void contains_includesBothEnds() {
        PriceRange priceRange = range("1000", "2000");

        assertTrue(priceRange.contains(new Price("1500")));
        assertTrue(priceRange.contains(new Price("1000")));
        assertTrue(priceRange.contains(new Price("2000")));
        assertFalse(priceRange.contains(new Price("999")));
        assertFalse(priceRange.contains(new Price("2001")));
    }

    @Test
    public void contains_singlePriceRange_matchesOnlyThatPrice() {
        PriceRange priceRange = new PriceRange(new Price("1500"));

        assertTrue(priceRange.contains(new Price("1500")));
        assertFalse(priceRange.contains(new Price("1499")));
        assertFalse(priceRange.contains(new Price("1501")));
    }

    @Test
    public void overlaps_budgetSharingAPrice_returnsTrue() {
        PriceRange priceRange = range("1000", "2000");

        // budget inside the range, around the range, and crossing either end
        assertTrue(priceRange.overlaps(budget("1200", "1800")));
        assertTrue(priceRange.overlaps(budget("500", "3000")));
        assertTrue(priceRange.overlaps(budget("500", "1500")));
        assertTrue(priceRange.overlaps(budget("1500", "3000")));

        // budget that only touches an end of the range
        assertTrue(priceRange.overlaps(budget("500", "1000")));
        assertTrue(priceRange.overlaps(budget("2000", "3000")));
    }

    @Test
    public void overlaps_budgetSharingNoPrice_returnsFalse() {
        PriceRange priceRange = range("1000", "2000");

        assertFalse(priceRange.overlaps(budget("500", "999")));
        assertFalse(priceRange.overlaps(budget("2001", "3000")));
    }

    @Test
    public void overlaps_singlePriceRange_matchesBudgetsThatIncludeThePrice() {
        PriceRange priceRange = new PriceRange(new Price("1500"));

        assertTrue(priceRange.overlaps(budget("1000", "2000")));
        assertTrue(priceRange.overlaps(budget("1500", "1500")));
        assertFalse(priceRange.overlaps(budget("1000", "1499")));
        assertFalse(priceRange.overlaps(budget("1501", "2000")));
    }

    @Test
    public void toString_rangeAndSinglePrice() {
        assertEquals("$800,000 to $1,200,000", range("800000", "1200000").toString());
        assertEquals("$850,000", new PriceRange(new Price("850000")).toString());
    }

    @Test
    public void equals() {
        PriceRange priceRange = range("1000", "2000");

        // same object -> returns true
        assertTrue(priceRange.equals(priceRange));

        // same values -> returns true, with the same hashcode
        assertTrue(priceRange.equals(range("1000", "2000")));
        assertEquals(priceRange.hashCode(), range("1000", "2000").hashCode());

        // a single price equals a range with the same ends
        assertTrue(new PriceRange(new Price("1500")).equals(range("1500", "1500")));

        // null -> returns false
        assertFalse(priceRange.equals(null));

        // different types -> returns false
        assertFalse(priceRange.equals(5));

        // different minimum or maximum -> returns false
        assertFalse(priceRange.equals(range("1001", "2000")));
        assertFalse(priceRange.equals(range("1000", "2001")));
    }
}
