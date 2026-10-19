package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Address;
import seedu.address.model.person.Area;
import seedu.address.model.person.Budget;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Email;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Price;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";
    private static final String INVALID_ROLE = "landlord";
    private static final String INVALID_NOTE = "a".repeat(Note.MAX_LENGTH + 1);
    private static final String INVALID_AREA = "Atlantis";
    private static final String INVALID_HOUSING_TYPE = "Castle";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final String VALID_ROLE = ClientRole.BUYER.getValue();
    private static final String VALID_NOTE = BENSON.getNote().value;
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        null,
                        VALID_TAGS, null, null, null, null);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        null,
                        VALID_TAGS, null, null, null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        null,
                        VALID_TAGS, null, null, null, null);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_ADDRESS, null,
                        null,
                        VALID_TAGS, null, null, null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_ADDRESS, null,
                        null,
                        VALID_TAGS, null, null, null, null);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_ADDRESS, null,
                        null,
                        VALID_TAGS, null, null, null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_ADDRESS, null,
                        null,
                        VALID_TAGS, null, null, null, null);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, null, null,
                        null,
                        VALID_TAGS, null, null, null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_validRole_returnsPersonWithThatRole() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_ROLE,
                        VALID_NOTE,
                        VALID_TAGS, null, null, null, null);
        Person expected = new PersonBuilder(BENSON).withRole(ClientRole.BUYER)
                .withBudget(null).withAskingPrice(null).withAreas(Set.of()).withHousingTypes(Set.of())
                .build();
        assertEquals(expected, person.toModelType());
    }

    @Test
    public void toModelType_nullRole_returnsPersonWithNoRole() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        VALID_NOTE,
                        VALID_TAGS, null, null, null, null);
        Person expected = new PersonBuilder(BENSON).withRole(null)
                .withBudget(null).withAskingPrice(null).withAreas(Set.of()).withHousingTypes(Set.of())
                .build();
        assertEquals(expected, person.toModelType());
    }

    @Test
    public void toModelType_invalidRole_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, INVALID_ROLE,
                        null,
                        VALID_TAGS, null, null, null, null);
        String expectedMessage = ClientRole.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullNote_returnsPersonWithNoNote() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        null, VALID_TAGS, null, null, null, null);
        assertNull(person.toModelType().getNote());
    }

    @Test
    public void toModelType_invalidNote_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        INVALID_NOTE, VALID_TAGS, null, null, null, null);
        assertThrows(IllegalValueException.class, Note.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_multilineNote_keepsTheNote() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        VALID_NOTE, VALID_TAGS, null, null, null, null);
        assertEquals(VALID_NOTE, person.toModelType().getNote().value);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        null,
                        invalidTags, null, null, null, null);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    @Test
    public void toModelType_missingRequirements_returnsEmptyRequirements() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        VALID_NOTE,
                        VALID_TAGS, null, null, null, null);
        Person expected = new PersonBuilder(BENSON).withRole(null)
                .withBudget(null).withAskingPrice(null).withAreas(Set.of()).withHousingTypes(Set.of())
                .build();
        assertEquals(expected, person.toModelType());
    }

    @Test
    public void toModelType_validRequirements_returnsPerson() throws Exception {
        Person person = new PersonBuilder(BENSON)
                .withBudget(new Budget(new Price("800000"), new Price("1200000")))
                .withAskingPrice(new Price("950000"))
                .withAreas(Set.of(new Area("Bishan"), new Area("Toa Payoh")))
                .withHousingTypes(Set.of(HousingType.HDB, HousingType.CONDO))
                .build();
        assertEquals(person, new JsonAdaptedPerson(person).toModelType());
    }

    @Test
    public void jsonRoundTrip_allRequirementsAndFiveHundredCharacterMultilineNote_preservesAllFields()
            throws Exception {
        String note = "a".repeat(249) + "\n" + "b".repeat(250);
        Person person = new PersonBuilder(BENSON)
                .withNote(note)
                .withBudget(new Budget(new Price("800000"), new Price("1200000")))
                .withAskingPrice(new Price("950000"))
                .withAreas(Set.of(new Area("Bishan"), new Area("Toa Payoh")))
                .withHousingTypes(Set.of(HousingType.HDB, HousingType.CONDO))
                .build();

        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(person));
        JsonAdaptedPerson personFromJson = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        Person modelPerson = personFromJson.toModelType();

        assertEquals(person, modelPerson);
        assertEquals(Note.MAX_LENGTH, modelPerson.getNote().value.length());
    }

    @Test
    public void toModelType_invalidBudget_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        null,
                        VALID_TAGS, new JsonAdaptedPerson.JsonAdaptedBudget(
                                BigDecimal.valueOf(2000), BigDecimal.valueOf(1000)), null, null, null);
        assertThrows(IllegalValueException.class, Budget.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_budgetMissingMin_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        null,
                        VALID_TAGS, new JsonAdaptedPerson.JsonAdaptedBudget(
                                null, BigDecimal.valueOf(1200000)), null, null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Budget.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_budgetMissingMax_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        null,
                        VALID_TAGS, new JsonAdaptedPerson.JsonAdaptedBudget(
                                BigDecimal.valueOf(800000), null), null, null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Budget.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidAskingPrice_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        null,
                        VALID_TAGS, null, BigDecimal.ZERO, null, null);
        assertThrows(IllegalValueException.class, Price.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_fractionalAskingPrice_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        null,
                        VALID_TAGS, null, new BigDecimal("100.99"), null, null);
        assertThrows(IllegalValueException.class, Price.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_fractionalJsonAskingPrice_throwsIllegalValueException() throws Exception {
        String json = """
                {
                  "name": "%s",
                  "phone": "%s",
                  "email": "%s",
                  "address": "%s",
                  "askingPrice": 100.99
                }
                """.formatted(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS);
        JsonAdaptedPerson person = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, Price.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_fractionalBudgetMin_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        null,
                        VALID_TAGS, new JsonAdaptedPerson.JsonAdaptedBudget(
                                new BigDecimal("800000.99"), BigDecimal.valueOf(1200000)),
                        null, null, null);
        assertThrows(IllegalValueException.class, Price.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_fractionalBudgetMax_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        null,
                        VALID_TAGS, new JsonAdaptedPerson.JsonAdaptedBudget(
                                BigDecimal.valueOf(800000), new BigDecimal("1200000.99")),
                        null, null, null);
        assertThrows(IllegalValueException.class, Price.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_fractionalJsonBudget_throwsIllegalValueException() throws Exception {
        String json = """
                {
                  "name": "%s",
                  "phone": "%s",
                  "email": "%s",
                  "address": "%s",
                  "budget": {
                    "min": 800000.99,
                    "max": 1200000
                  }
                }
                """.formatted(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS);
        JsonAdaptedPerson person = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertThrows(IllegalValueException.class, Price.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_invalidArea_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        null,
                        VALID_TAGS, null, null, List.of(INVALID_AREA), null);
        assertThrows(IllegalValueException.class, Area.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_invalidHousingType_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                        null,
                        VALID_TAGS, null, null, null, List.of(INVALID_HOUSING_TYPE));
        assertThrows(IllegalValueException.class, HousingType.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_personWithRole_keepsTheRole() throws Exception {
        Person personWithRole = new PersonBuilder(BENSON).withRole(ClientRole.SELLER).build();
        assertEquals(personWithRole, new JsonAdaptedPerson(personWithRole).toModelType());
    }

    @Test
    public void toJsonString_personWithNoRole_leavesOutTheRoleKey() throws Exception {
        Person personWithoutRole = new PersonBuilder(BENSON).withRole(null).build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(personWithoutRole));
        assertFalse(json.contains("role"));
    }

    @Test
    public void toJsonString_personWithNoNote_leavesOutTheNoteKey() throws Exception {
        Person personWithNoNote = new PersonBuilder().build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(personWithNoNote));
        assertFalse(json.contains("note"));
    }

    @Test
    public void toJsonString_requirements_areNormalized() throws Exception {
        Person person = new PersonBuilder(BENSON)
                .withBudget(new Budget(new Price("1,000,000"), new Price("1,200,000")))
                .withAskingPrice(new Price("1,100,000"))
                .build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(person));

        assertTrue(json.contains("1000000"));
        assertTrue(json.contains("1200000"));
        assertTrue(json.contains("1100000"));
        assertFalse(json.contains("1,000,000"));
        assertFalse(json.contains("$"));
    }

}
