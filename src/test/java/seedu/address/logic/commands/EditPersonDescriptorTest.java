package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Area;
import seedu.address.model.person.Budget;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.Price;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditPersonDescriptorTest {

    @Test
    public void equals() {
        // same values -> returns true
        EditPersonDescriptor descriptorWithSameValues = new EditPersonDescriptor(DESC_AMY);
        assertTrue(DESC_AMY.equals(descriptorWithSameValues));

        // same object -> returns true
        assertTrue(DESC_AMY.equals(DESC_AMY));

        // null -> returns false
        assertFalse(DESC_AMY.equals(null));

        // different types -> returns false
        assertFalse(DESC_AMY.equals(5));

        // different values -> returns false
        assertFalse(DESC_AMY.equals(DESC_BOB));

        // different name -> returns false
        EditPersonDescriptor editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withName(VALID_NAME_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different phone -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withPhone(VALID_PHONE_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different email -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different address -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different tags -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different budget -> returns false
        EditPersonDescriptor editedBudget = new EditPersonDescriptorBuilder(DESC_AMY)
                .withBudget(new Budget(new Price("1000"), new Price("2000")))
                .build();
        assertFalse(DESC_AMY.equals(editedBudget));
        assertFalse(editedBudget.equals(DESC_AMY));

        // same non-null budget -> returns true
        EditPersonDescriptor budgetOne = new EditPersonDescriptorBuilder()
                .withBudget(new Budget(new Price("1000"), new Price("2000")))
                .build();
        EditPersonDescriptor budgetTwo = new EditPersonDescriptorBuilder()
                .withBudget(new Budget(new Price("1000"), new Price("2000")))
                .build();
        assertTrue(budgetOne.equals(budgetTwo));
        EditPersonDescriptor differentBudget = new EditPersonDescriptorBuilder()
                .withBudget(new Budget(new Price("3000"), new Price("4000")))
                .build();
        assertFalse(budgetOne.equals(differentBudget));

        // budget edited flag differs even when both values are null -> returns false
        EditPersonDescriptor clearedBudget = new EditPersonDescriptorBuilder(DESC_AMY).withBudget(null).build();
        assertFalse(DESC_AMY.equals(clearedBudget));

        // different asking price -> returns false
        EditPersonDescriptor editedPrice = new EditPersonDescriptorBuilder(DESC_AMY)
                .withAskingPrice(new Price("1500"))
                .build();
        assertFalse(DESC_AMY.equals(editedPrice));
        assertFalse(editedPrice.equals(DESC_AMY));

        // same non-null asking price -> returns true
        EditPersonDescriptor priceOne = new EditPersonDescriptorBuilder()
                .withAskingPrice(new Price("1500"))
                .build();
        EditPersonDescriptor priceTwo = new EditPersonDescriptorBuilder()
                .withAskingPrice(new Price("1500"))
                .build();
        assertTrue(priceOne.equals(priceTwo));
        EditPersonDescriptor differentPrice = new EditPersonDescriptorBuilder()
                .withAskingPrice(new Price("2500"))
                .build();
        assertFalse(priceOne.equals(differentPrice));

        // asking price edited flag differs even when both values are null -> returns false
        EditPersonDescriptor clearedPrice = new EditPersonDescriptorBuilder(DESC_AMY)
                .withAskingPrice(null).build();
        assertFalse(DESC_AMY.equals(clearedPrice));

        // different areas -> returns false
        EditPersonDescriptor editedAreas = new EditPersonDescriptorBuilder(DESC_AMY)
                .withAreas(Set.of(new Area("Orchard")))
                .build();
        assertFalse(DESC_AMY.equals(editedAreas));

        // different housing types -> returns false
        EditPersonDescriptor editedHousingTypes = new EditPersonDescriptorBuilder(DESC_AMY)
                .withHousingTypes(Set.of(HousingType.CONDO))
                .build();
        assertFalse(DESC_AMY.equals(editedHousingTypes));
    }

    @Test
    public void toStringMethod() {
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        String expected = EditPersonDescriptor.class.getCanonicalName() + "{name="
                + editPersonDescriptor.getName().orElse(null) + ", phone="
                + editPersonDescriptor.getPhone().orElse(null) + ", email="
                + editPersonDescriptor.getEmail().orElse(null) + ", address="
                + editPersonDescriptor.getAddress().orElse(null) + ", tags="
                + editPersonDescriptor.getTags().orElse(null) + ", budget="
                + editPersonDescriptor.getBudget().orElse(null) + ", budgetEdited="
                + editPersonDescriptor.isBudgetEdited() + ", askingPrice="
                + editPersonDescriptor.getAskingPrice().orElse(null) + ", askingPriceEdited="
                + editPersonDescriptor.isAskingPriceEdited() + ", areas="
                + editPersonDescriptor.getAreas().orElse(null) + ", housingTypes="
                + editPersonDescriptor.getHousingTypes().orElse(null) + "}";
        assertEquals(expected, editPersonDescriptor.toString());
    }
}
