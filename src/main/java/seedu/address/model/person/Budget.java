package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

/**
 * Represents a client's budget as a minimum and maximum price.
 * Guarantees: immutable; the minimum does not exceed the maximum.
 */
public class Budget {

    public static final String MESSAGE_CONSTRAINTS = "The budget minimum must not exceed the maximum.";

    private final Price min;
    private final Price max;

    /**
     * Constructs a {@code Budget}.
     *
     * @param min The minimum price.
     * @param max The maximum price.
     */
    public Budget(Price min, Price max) {
        requireAllNonNull(min, max);
        checkArgument(min.getValue() <= max.getValue(), MESSAGE_CONSTRAINTS);
        this.min = min;
        this.max = max;
    }

    public Price getMin() {
        return min;
    }

    public Price getMax() {
        return max;
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

        if (!(other instanceof Budget otherBudget)) {
            return false;
        }

        return min.equals(otherBudget.min) && max.equals(otherBudget.max);
    }

    @Override
    public int hashCode() {
        return 31 * min.hashCode() + max.hashCode();
    }
}
