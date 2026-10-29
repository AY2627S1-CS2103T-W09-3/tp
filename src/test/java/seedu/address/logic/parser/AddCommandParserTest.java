package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_PREFIX;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.AREA_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.AREA_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.ASKING_PRICE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ASKING_PRICE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.BUDGET_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.BUDGET_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.HOUSING_TYPE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.HOUSING_TYPE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_AREA_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_BUDGET_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_BUDGET_MONEY_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_HOUSING_TYPE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PRICE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ASKING_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BUDGET;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Address;
import seedu.address.model.person.Area;
import seedu.address.model.person.Budget;
import seedu.address.model.person.Email;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Price;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND).build();

        // whitespace only preamble
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND, new AddCommand(expectedPerson));


        // extra spaces inside the name - collapsed to single spaces
        assertParseSuccess(parser, " n/Bob  Choo" + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND, new AddCommand(expectedPerson));

        // multiple tags - all accepted
        Person expectedPersonMultipleTags = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND, VALID_TAG_HUSBAND)
                .build();
        assertParseSuccess(parser,
                NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                new AddCommand(expectedPersonMultipleTags));
    }

    @Test
    public void parse_repeatedNonTagValue_failure() {
        String validExpectedPersonString = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND;

        // multiple names
        assertParseFailure(parser, NAME_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // multiple phones
        assertParseFailure(parser, PHONE_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple emails
        assertParseFailure(parser, EMAIL_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // multiple addresses
        assertParseFailure(parser, ADDRESS_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // multiple fields repeated
        assertParseFailure(parser,
                validExpectedPersonString + PHONE_DESC_AMY + EMAIL_DESC_AMY + NAME_DESC_AMY + ADDRESS_DESC_AMY
                        + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_ADDRESS, PREFIX_EMAIL, PREFIX_PHONE));

        // invalid value followed by valid value

        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, INVALID_EMAIL_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, INVALID_PHONE_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid address
        assertParseFailure(parser, INVALID_ADDRESS_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // valid value followed by invalid value

        // invalid name
        assertParseFailure(parser, validExpectedPersonString + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, validExpectedPersonString + INVALID_EMAIL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, validExpectedPersonString + INVALID_PHONE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid address
        assertParseFailure(parser, validExpectedPersonString + INVALID_ADDRESS_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));
    }

    @Test
    public void parse_optionalFieldsMissing_success() {
        // zero tags
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        assertParseSuccess(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // missing name prefix
        assertParseFailure(parser, VALID_NAME_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB,
                expectedMessage);

        // missing phone prefix
        assertParseFailure(parser, NAME_DESC_BOB + VALID_PHONE_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB,
                expectedMessage);

        // missing email prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + VALID_EMAIL_BOB + ADDRESS_DESC_BOB,
                expectedMessage);

        // missing address prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + VALID_ADDRESS_BOB,
                expectedMessage);

        // all prefixes missing
        assertParseFailure(parser, VALID_NAME_BOB + VALID_PHONE_BOB + VALID_EMAIL_BOB + VALID_ADDRESS_BOB,
                expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Name.MESSAGE_CONSTRAINTS);

        // invalid phone
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Phone.MESSAGE_CONSTRAINTS);

        // invalid email
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC + ADDRESS_DESC_BOB
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Email.MESSAGE_CONSTRAINTS);

        // invalid address
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_ADDRESS_DESC
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Address.MESSAGE_CONSTRAINTS);

        // invalid tag
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + INVALID_TAG_DESC + VALID_TAG_FRIEND, Tag.MESSAGE_CONSTRAINTS);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_ADDRESS_DESC,
                Name.MESSAGE_CONSTRAINTS);

        // non-empty preamble
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_requirementFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).withTags()
                .withBudget(new Budget(new Price("2000"), new Price("3000")))
                .withAskingPrice(new Price("2500"))
                .withAreas(Set.of(new Area("Orchard")))
                .withHousingTypes(Set.of(HousingType.CONDO))
                .build();

        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + BUDGET_DESC_BOB + ASKING_PRICE_DESC_BOB + AREA_DESC_BOB + HOUSING_TYPE_DESC_BOB,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_eachRequirementFieldAlone_success() {
        String validPerson = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;

        Person expectedBudget = new PersonBuilder(BOB).withTags()
                .withBudget(new Budget(new Price("1000"), new Price("2000")))
                .build();
        assertParseSuccess(parser, validPerson + BUDGET_DESC_AMY, new AddCommand(expectedBudget));

        Person expectedPrice = new PersonBuilder(BOB).withTags().withAskingPrice(new Price("1500")).build();
        assertParseSuccess(parser, validPerson + ASKING_PRICE_DESC_AMY, new AddCommand(expectedPrice));

        Person expectedArea = new PersonBuilder(BOB).withTags()
                .withAreas(Set.of(new Area("Bukit Timah"))).build();
        assertParseSuccess(parser, validPerson + AREA_DESC_AMY, new AddCommand(expectedArea));

        Person expectedHousingType = new PersonBuilder(BOB).withTags()
                .withHousingTypes(Set.of(HousingType.HDB)).build();
        assertParseSuccess(parser, validPerson + HOUSING_TYPE_DESC_AMY, new AddCommand(expectedHousingType));
    }

    @Test
    public void parse_repeatedAreaAndHousingType_success() {
        Person expectedPerson = new PersonBuilder(BOB).withTags()
                .withAreas(Set.of(new Area("Orchard")))
                .withHousingTypes(Set.of(HousingType.CONDO))
                .build();

        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + " l/Orchard l/orchard h/Condo h/condo", new AddCommand(expectedPerson));
    }

    @Test
    public void parse_mistypedRequirementPrefix_failure() {
        String validPerson = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;
        String expected = String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "bb/") + "\n"
                + AddCommand.MESSAGE_USAGE;

        assertParseFailure(parser, validPerson + " bb/400000-600000", expected);
    }

    @Test
    public void parse_doubledLetterPrefix_failure() {
        String validPerson = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;
        String expected = String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "pp/") + "\n"
                + AddCommand.MESSAGE_USAGE;

        assertParseFailure(parser, validPerson + " pp/91234567", expected);
    }

    @Test
    public void parse_slashInAddressText_success() {
        String addressWithSlash = "12 Main Street c/o Mary Tan";
        Person expectedPerson = new PersonBuilder(BOB).withAddress(addressWithSlash).withTags().build();

        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + " a/" + addressWithSlash,
                new AddCommand(expectedPerson));

        String addressWithLift = "7 Sunset Way w/ lift";
        Person expectedLiftPerson = new PersonBuilder(BOB).withAddress(addressWithLift).withTags().build();

        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + " a/" + addressWithLift,
                new AddCommand(expectedLiftPerson));

        String addressWithSonOf = "12 Main Street s/o Ravi";
        Person expectedSonOfPerson = new PersonBuilder(BOB).withAddress(addressWithSonOf).withTags().build();

        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + " a/" + addressWithSonOf,
                new AddCommand(expectedSonOfPerson));

        String addressWithBecauseOf = "12 Main Street b/c Tall";
        Person expectedBecauseOfPerson = new PersonBuilder(BOB).withAddress(addressWithBecauseOf).withTags().build();

        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + " a/" + addressWithBecauseOf,
                new AddCommand(expectedBecauseOfPerson));
    }

    @Test
    public void parse_shortWordAfterNumericPrefix_failure() {
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + " a/12 Main Street s/ab",
                Price.MESSAGE_CONSTRAINTS);

        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + " a/7 Sunset Way b/xx",
                Price.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_areaWithRepeatedInternalWhitespace_success() {
        String validPerson = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;
        Person expectedPerson = new PersonBuilder(BOB).withTags()
                .withAreas(Set.of(new Area("Ang Mo Kio")))
                .build();

        assertParseSuccess(parser, validPerson + " l/Ang  mo  kio", new AddCommand(expectedPerson));
    }

    @Test
    public void parse_repeatedBudgetOrPrice_failure() {
        String validPerson = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;

        assertParseFailure(parser, validPerson + BUDGET_DESC_AMY + BUDGET_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_BUDGET));
        assertParseFailure(parser, validPerson + ASKING_PRICE_DESC_AMY + ASKING_PRICE_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ASKING_PRICE));
    }

    @Test
    public void parse_invalidRequirement_failure() {
        String validPerson = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;

        assertParseFailure(parser, validPerson + INVALID_BUDGET_MONEY_DESC, Price.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validPerson + INVALID_BUDGET_DESC, Budget.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validPerson + INVALID_PRICE_DESC, Price.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validPerson + INVALID_AREA_DESC, Area.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, validPerson + INVALID_HOUSING_TYPE_DESC, HousingType.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_addressWithSlash_success() {
        String addressWithSlash = " a/12 Jalan/Street";
        Person expectedPerson = new PersonBuilder(BOB).withTags().withAddress("12 Jalan/Street").build();

        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + addressWithSlash,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_uppercaseOrForeignPrefix_failure() {
        String validPerson = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB;

        assertParseFailure(parser, validPerson + " B/400000-600000",
                String.format(MESSAGE_UNKNOWN_PREFIX, "B/") + "\n" + AddCommand.MESSAGE_USAGE);
        assertParseFailure(parser, validPerson + " r/buyer",
                String.format(MESSAGE_UNKNOWN_PREFIX, "r/") + "\n" + AddCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_freeTextAbbreviationInAnyCase_success() {
        Person expectedUpper = new PersonBuilder(BOB).withTags().withAddress("12 Main Street S/o Ravi").build();
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + " a/12 Main Street S/o Ravi",
                new AddCommand(expectedUpper));

        Person expectedLower = new PersonBuilder(BOB).withTags().withAddress("12 Main Street s/o Ravi").build();
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + " a/12 Main Street s/o Ravi",
                new AddCommand(expectedLower));
    }
}
