package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PriceTest {

    @Test
    public void isValidPrice_validPrices_returnsTrue() {
        assertTrue(Price.isValidPrice("1"));
        assertTrue(Price.isValidPrice("999"));
        assertTrue(Price.isValidPrice("1000"));
        assertTrue(Price.isValidPrice("1,000"));
        assertTrue(Price.isValidPrice("1000000"));
        assertTrue(Price.isValidPrice("1,000,000"));
        assertTrue(Price.isValidPrice("1000000000"));
        assertTrue(Price.isValidPrice("1,000,000,000"));
    }

    @Test
    public void isValidPrice_invalidPrices_returnsFalse() {
        assertFalse(Price.isValidPrice("0"));
        assertFalse(Price.isValidPrice("-1"));
        assertFalse(Price.isValidPrice("+1"));
        assertFalse(Price.isValidPrice("1.0"));
        assertFalse(Price.isValidPrice("$1000"));
        assertFalse(Price.isValidPrice("1k"));
        assertFalse(Price.isValidPrice("10,00,000"));
        assertFalse(Price.isValidPrice("1,00,000"));
        assertFalse(Price.isValidPrice("01"));
        assertFalse(Price.isValidPrice("1000000001"));
        assertFalse(Price.isValidPrice("1,000,000,000,000"));
        assertFalse(Price.isValidPrice(""));
        assertFalse(Price.isValidPrice(" "));
        assertFalse(Price.isValidPrice(null));
    }

    @Test
    public void constructor_invalidPrice_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Price.MESSAGE_CONSTRAINTS, () -> new Price("0"));
    }

    @Test
    public void isValidPrice_oversizedNumber_returnsFalse() {
        assertFalse(Price.isValidPrice("10000000000"));
        assertFalse(Price.isValidPrice("999999999999999999999"));
    }

    @Test
    public void constructor_oversizedPrice_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Price.MESSAGE_CONSTRAINTS, ()
                -> new Price("999999999999999999999"));
    }

    @Test
    public void toString_returnsDollarAmountWithCommas() {
        assertEquals("$1", new Price("1").toString());
        assertEquals("$1,000,000", new Price("1,000,000").toString());
        assertEquals("$1,000,000,000", new Price("1000000000").toString());
    }

    @Test
    public void equals() {
        Price price = new Price("1,000,000");

        assertTrue(price.equals(new Price("1000000")));
        assertTrue(price.equals(price));
        assertFalse(price.equals(new Price("999999")));
        assertFalse(price.equals(null));
        assertFalse(price.equals("1000000"));
        assertEquals(price.hashCode(), new Price("1000000").hashCode());
    }
}
