package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class BudgetTest {

    @Test
    public void toString_rangeAndSingleValue() {
        Budget budget = new Budget(new Price("800000"), new Price("1200000"));
        Budget singleValueBudget = new Budget(new Price("800000"), new Price("800000"));

        assertEquals("$800,000 to $1,200,000", budget.toString());
        assertEquals("$800,000", singleValueBudget.toString());
    }

    @Test
    public void constructor_minimumAboveMaximum_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Budget.MESSAGE_CONSTRAINTS, ()
                -> new Budget(new Price("1200000"), new Price("800000")));
    }

    @Test
    public void equals() {
        Budget budget = new Budget(new Price("800000"), new Price("1200000"));

        assertTrue(budget.equals(new Budget(new Price("800000"), new Price("1200000"))));
        assertTrue(budget.equals(budget));
        assertFalse(budget.equals(new Budget(new Price("800000"), new Price("1100000"))));
        assertFalse(budget.equals(null));
        assertEquals(budget.hashCode(), new Budget(new Price("800000"), new Price("1200000")).hashCode());
    }
}
