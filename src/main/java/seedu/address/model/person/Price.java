package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a property price in whole Singapore dollars.
 * Guarantees: immutable; is valid as declared in {@link #isValidPrice(String)}.
 */
public class Price {

    public static final String MESSAGE_CONSTRAINTS =
            "Prices must be whole numbers from 1 to 1,000,000,000, with optional correctly placed commas.";

    public static final String VALIDATION_REGEX = "(?:[1-9]\\d{0,2}(?:,\\d{3})+|[1-9]\\d*)";

    private static final long MAX_PRICE = 1_000_000_000L;
    private static final int MAX_PRICE_DIGITS = Long.toString(MAX_PRICE).length();

    private final int value;

    /**
     * Constructs a {@code Price}.
     *
     * @param price A valid price.
     */
    public Price(String price) {
        requireNonNull(price);
        checkArgument(isValidPrice(price), MESSAGE_CONSTRAINTS);
        value = Integer.parseInt(price.replace(",", ""));
    }

    /**
     * Returns true if a given string is a valid price.
     */
    public static boolean isValidPrice(String test) {
        if (test == null || !test.matches(VALIDATION_REGEX)) {
            return false;
        }
        String digits = test.replace(",", "");
        if (digits.length() > MAX_PRICE_DIGITS) {
            return false;
        }
        long parsed = Long.parseLong(digits);
        return parsed >= 1 && parsed <= MAX_PRICE;
    }

    /**
     * Returns the price in whole dollars.
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the price in the form shown to the user, for example {@code $1,000,000}.
     */
    public String getDisplayValue() {
        return "$" + String.format(Locale.ROOT, "%,d", value);
    }

    @Override
    public String toString() {
        return getDisplayValue();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Price otherPrice)) {
            return false;
        }

        return value == otherPrice.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}
