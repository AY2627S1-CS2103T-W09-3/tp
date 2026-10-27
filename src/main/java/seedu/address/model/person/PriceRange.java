package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

/**
 * Represents a range of prices from a minimum to a maximum, both included, that clients are filtered by.
 * Guarantees: immutable; the minimum does not exceed the maximum.
 */
public class PriceRange {

    public static final String MESSAGE_CONSTRAINTS = "The minimum price must not exceed the maximum.";
    public static final String MESSAGE_INVALID_FORMAT =
            "A price range should be one price, or a minimum and a maximum price joined by a hyphen, "
            + "such as 500000-600000.";

    private final Price min;
    private final Price max;

    /**
     * Constructs a {@code PriceRange}.
     *
     * @param min The minimum price.
     * @param max The maximum price.
     */
    public PriceRange(Price min, Price max) {
        requireAllNonNull(min, max);
        checkArgument(min.getValue() <= max.getValue(), MESSAGE_CONSTRAINTS);
        this.min = min;
        this.max = max;
    }

    /**
     * Constructs a {@code PriceRange} that holds only the given {@code price}.
     */
    public PriceRange(Price price) {
        this(price, price);
    }

    /**
     * Returns true if the given {@code price} is within this range, including both ends.
     */
    public boolean contains(Price price) {
        requireNonNull(price);
        return min.getValue() <= price.getValue() && price.getValue() <= max.getValue();
    }

    /**
     * Returns true if this range and the given {@code budget} share at least one price, including their ends.
     */
    public boolean overlaps(Budget budget) {
        requireNonNull(budget);
        return min.getValue() <= budget.getMax().getValue() && budget.getMin().getValue() <= max.getValue();
    }

    @Override
    public String toString() {
        if (min.equals(max)) {
            return min.toString();
        }
        return min + " to " + max;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof PriceRange otherPriceRange)) {
            return false;
        }

        return min.equals(otherPriceRange.min) && max.equals(otherPriceRange.max);
    }

    @Override
    public int hashCode() {
        return 31 * min.hashCode() + max.hashCode();
    }
}
