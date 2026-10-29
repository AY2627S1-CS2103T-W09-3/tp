package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.model.util.SampleDataUtil;
import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void constructor_nullRole_isAllowed() {
        Person personWithNoRole = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                ALICE.getAddress(), null, ALICE.getNote(), ALICE.getTags(),
                ALICE.getBudget(), ALICE.getAskingPrice(),
                ALICE.getAreas(), ALICE.getHousingTypes());
        assertNull(personWithNoRole.getRole());
    }

    @Test
    public void constructor_nullNote_isAllowed() {
        Person personWithNoNote = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                ALICE.getAddress(), ALICE.getRole(), null, ALICE.getTags(),
                ALICE.getBudget(), ALICE.getAskingPrice(), ALICE.getAreas(), ALICE.getHousingTypes());
        assertNull(personWithNoNote.getNote());
    }

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // same phone, all other attributes different -> returns true
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // same email, all other attributes different -> returns true
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // email differs in domain case, all other attributes different -> returns true
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withEmail(ALICE.getEmail().value.toUpperCase()).withAddress(VALID_ADDRESS_BOB)
                .withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different name, phone and email, same address and tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, phone and email different -> returns true
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase())
                .withPhone(VALID_PHONE_AMY).withEmail(VALID_EMAIL_AMY).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has trailing spaces, phone and email different -> returns true
        editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB + " ")
                .withPhone(VALID_PHONE_AMY).withEmail(VALID_EMAIL_AMY).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has extra internal space, phone and email different -> returns true
        editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.replace(" ", "  "))
                .withPhone(VALID_PHONE_AMY).withEmail(VALID_EMAIL_AMY).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has extra internal space and differs in case, phone and email different -> returns true
        editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase().replace(" ", "   "))
                .withPhone(VALID_PHONE_AMY).withEmail(VALID_EMAIL_AMY).build();
        assertTrue(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));
        assertEquals(ALICE.hashCode(), aliceCopy.hashCode());

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));

        // tag differs only in case -> returns false because the displayed data is different
        Person lowerCaseTagAlice = new PersonBuilder(ALICE).withTags("urgent").build();
        Person upperCaseTagAlice = new PersonBuilder(ALICE).withTags("URGENT").build();
        assertFalse(lowerCaseTagAlice.equals(upperCaseTagAlice));

        // different role -> different details, but still the same person
        editedAlice = new PersonBuilder(ALICE).withRole(ClientRole.SELLER).build();
        assertFalse(ALICE.equals(editedAlice));
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different note -> different details, but still the same person
        editedAlice = new PersonBuilder(ALICE).withNote("Call after 6 PM").build();
        assertFalse(ALICE.equals(editedAlice));
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different budget -> returns false
        editedAlice = new PersonBuilder(ALICE)
                .withBudget(new Budget(new Price("900000"), new Price("1000000"))).build();
        assertFalse(ALICE.equals(editedAlice));

        // different asking price -> returns false
        editedAlice = new PersonBuilder(ALICE).withAskingPrice(new Price("900000")).build();
        assertFalse(ALICE.equals(editedAlice));

        // different areas -> returns false
        editedAlice = new PersonBuilder(ALICE)
                .withAreas(SampleDataUtil.getAreaSet("Bedok")).build();
        assertFalse(ALICE.equals(editedAlice));

        // different housing types -> returns false
        editedAlice = new PersonBuilder(ALICE)
                .withHousingTypes(SampleDataUtil.getHousingTypeSet("EC")).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", address=" + ALICE.getAddress()
                + ", role=" + ALICE.getRole() + ", note=" + ALICE.getNote()
                + ", tags=" + ALICE.getTags()
                + ", budget=" + ALICE.getBudget() + ", askingPrice=" + ALICE.getAskingPrice()
                + ", areas=" + ALICE.getAreas() + ", housingTypes=" + ALICE.getHousingTypes() + "}";
        assertEquals(expected, ALICE.toString());
    }
}
