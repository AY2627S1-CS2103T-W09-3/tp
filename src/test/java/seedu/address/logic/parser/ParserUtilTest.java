package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_PREFIX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_AREA;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ASKING_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BUDGET;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_HOUSING_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Area;
import seedu.address.model.person.Budget;
import seedu.address.model.person.Email;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Price;
import seedu.address.model.person.PriceRange;
import seedu.address.model.tag.Tag;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "123456";
    private static final String VALID_ADDRESS = "123 Main Street #0505";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_TAG_1 = "friend";
    private static final String VALID_TAG_2 = "neighbour";
    private static final String VALID_BUDGET = "1,000-2,000";
    private static final String VALID_PRICE = "1,500";
    private static final String VALID_AREA = "Bukit Timah";
    private static final String VALID_HOUSING_TYPE = "hdb";
    private static final String INVALID_BUDGET = "2000-1000";
    private static final String INVALID_PRICE = "0";
    private static final String INVALID_AREA = "Atlantis";
    private static final String INVALID_HOUSING_TYPE = "Apartment";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parseName_validValueWithExtraInternalSpaces_returnsNameWithSingleSpaces() throws Exception {
        Name expectedName = new Name("Alpha Tester");
        assertEquals(expectedName, ParserUtil.parseName("Alpha  Tester"));
        assertEquals(expectedName, ParserUtil.parseName("Alpha     Tester"));
        assertEquals(expectedName, ParserUtil.parseName(WHITESPACE + "Alpha   Tester" + WHITESPACE));
        assertEquals(new Name("Alpha Tester Lim"), ParserUtil.parseName("Alpha  Tester   Lim"));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseAddress_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddress((String) null));
    }

    @Test
    public void parseAddress_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(INVALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithoutWhitespace_returnsAddress() throws Exception {
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(VALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithWhitespace_returnsTrimmedAddress() throws Exception {
        String addressWithWhitespace = WHITESPACE + VALID_ADDRESS + WHITESPACE;
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(addressWithWhitespace));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseNote_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseNote(null));
    }

    @Test
    public void parseNote_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseNote("   "));
        assertThrows(ParseException.class, () -> ParserUtil.parseNote("a".repeat(Note.MAX_LENGTH + 1)));
    }

    @Test
    public void parseNote_escapedNewline_decodesNewline() throws Exception {
        assertEquals(new Note("First line\nSecond line"), ParserUtil.parseNote("First line\\nSecond line"));
    }

    @Test
    public void parseNote_escapedBackslashes_decodesLiteralBackslashes() throws Exception {
        assertEquals(new Note("Saved at C:\\new folder\\notes"),
                ParserUtil.parseNote("Saved at C:\\\\new folder\\\\notes"));
    }

    @Test
    public void parseNote_unknownAndTrailingEscapes_preservesBackslashes() throws Exception {
        assertEquals(new Note("Keep \\t literal and trailing \\"),
                ParserUtil.parseNote("Keep \\t literal and trailing \\"));
    }

    @Test
    public void parseTag_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTag(null));
    }

    @Test
    public void parseTag_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTag(INVALID_TAG));
    }

    @Test
    public void parseTag_validValueWithoutWhitespace_returnsTag() throws Exception {
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(VALID_TAG_1));
    }

    @Test
    public void parseTag_validValueWithWhitespace_returnsTrimmedTag() throws Exception {
        String tagWithWhitespace = WHITESPACE + VALID_TAG_1 + WHITESPACE;
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(tagWithWhitespace));
    }

    @Test
    public void parseTags_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(null));
    }

    @Test
    public void parseTags_collectionWithInvalidTags_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTags(List.of(VALID_TAG_1, INVALID_TAG)));
    }

    @Test
    public void parseTags_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseTags(List.of()).isEmpty());
    }

    @Test
    public void parseTags_collectionWithValidTags_returnsTagSet() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of(VALID_TAG_1, VALID_TAG_2));
        Set<Tag> expectedTagSet = Set.of(new Tag(VALID_TAG_1), new Tag(VALID_TAG_2));

        assertEquals(expectedTagSet, actualTagSet);
    }

    @Test
    public void parseTags_caseVariantDuplicates_returnsSingleTag() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of("urgent", "URGENT"));

        assertEquals(Set.of(new Tag("urgent")), actualTagSet);
    }

    @Test
    public void parseBudget_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseBudget(null));
    }

    @Test
    public void parseBudget_invalidMoney_throwsParseException() {
        assertThrows(ParseException.class, Price.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseBudget("1000"));
        assertThrows(ParseException.class, Price.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseBudget("1000-2000-3000"));
    }

    @Test
    public void parseBudget_reversedRange_throwsParseException() {
        assertThrows(ParseException.class, Budget.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseBudget(INVALID_BUDGET));
    }

    @Test
    public void parseBudget_validRange_returnsBudget() throws Exception {
        Budget expectedBudget = new Budget(new Price("1000"), new Price("2000"));
        assertEquals(expectedBudget, ParserUtil.parseBudget(VALID_BUDGET));
        assertEquals(expectedBudget, ParserUtil.parseBudget(WHITESPACE + "1000 - 2000" + WHITESPACE));
    }

    @Test
    public void parsePriceRange_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePriceRange(null, "budget"));
    }

    @Test
    public void parsePriceRange_invalidPrice_throwsParseException() {
        for (String input : List.of("", "  ", "abc", "0", "abc-2000", "1000-abc", "1,00-2000", "1000-2000.5")) {
            assertThrows(ParseException.class, Price.MESSAGE_CONSTRAINTS, () ->
                    ParserUtil.parsePriceRange(input, "budget"));
        }
    }

    @Test
    public void parsePriceRange_malformedRange_throwsParseException() {
        for (String input : List.of("1000-", "-1000", "-", "1000 -", "1000-2000-3000")) {
            assertThrows(ParseException.class, PriceRange.MESSAGE_INVALID_FORMAT, () ->
                    ParserUtil.parsePriceRange(input, "budget"));
        }
    }

    @Test
    public void parsePriceRange_reversedRange_throwsParseException() {
        assertThrows(ParseException.class, String.format(PriceRange.MESSAGE_REVERSED_RANGE, "budget"), () ->
                ParserUtil.parsePriceRange("2000-1000", "budget"));
        assertThrows(ParseException.class, String.format(PriceRange.MESSAGE_REVERSED_RANGE, "asking price"), () ->
                ParserUtil.parsePriceRange("2000-1000", "asking price"));
    }

    @Test
    public void parsePriceRange_validRange_returnsPriceRange() throws Exception {
        PriceRange expectedPriceRange = new PriceRange(new Price("1000"), new Price("2000"));

        assertEquals(expectedPriceRange, ParserUtil.parsePriceRange("1000-2000", "budget"));
        assertEquals(expectedPriceRange, ParserUtil.parsePriceRange("1,000-2,000", "budget"));
        assertEquals(expectedPriceRange,
                ParserUtil.parsePriceRange(WHITESPACE + "1000 - 2000" + WHITESPACE, "budget"));
        assertEquals(new PriceRange(new Price("1500")), ParserUtil.parsePriceRange("1500-1500", "budget"));
    }

    @Test
    public void parsePriceRange_singlePrice_returnsRangeHoldingOnlyThatPrice() throws Exception {
        PriceRange expectedPriceRange = new PriceRange(new Price("1500"));

        assertEquals(expectedPriceRange, ParserUtil.parsePriceRange("1500", "budget"));
        assertEquals(expectedPriceRange, ParserUtil.parsePriceRange("1,500", "budget"));
        assertEquals(expectedPriceRange, ParserUtil.parsePriceRange(WHITESPACE + "1500" + WHITESPACE, "budget"));
    }

    @Test
    public void parsePrice_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePrice(null));
    }

    @Test
    public void parsePrice_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Price.MESSAGE_CONSTRAINTS, () -> ParserUtil.parsePrice(INVALID_PRICE));
    }

    @Test
    public void parsePrice_validValue_returnsPrice() throws Exception {
        assertEquals(new Price("1500"), ParserUtil.parsePrice(VALID_PRICE));
        assertEquals(new Price("1500"), ParserUtil.parsePrice(WHITESPACE + VALID_PRICE + WHITESPACE));
    }

    @Test
    public void parseArea_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseArea(null));
    }

    @Test
    public void parseArea_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Area.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseArea(INVALID_AREA));
    }

    @Test
    public void parseArea_validValue_returnsArea() throws Exception {
        assertEquals(new Area(VALID_AREA), ParserUtil.parseArea(VALID_AREA.toLowerCase()));
        assertEquals(new Area(VALID_AREA), ParserUtil.parseArea(WHITESPACE + VALID_AREA + WHITESPACE));
    }

    @Test
    public void parseAreas_collectionWithValidAreas_returnsDeduplicatedSet() throws Exception {
        Set<Area> actualAreas = ParserUtil.parseAreas(List.of("Orchard", "orchard", VALID_AREA));
        Set<Area> expectedAreas = Set.of(new Area("Orchard"), new Area(VALID_AREA));

        assertEquals(expectedAreas, actualAreas);
    }

    @Test
    public void parseAreas_collectionWithInvalidArea_throwsParseException() {
        assertThrows(ParseException.class, Area.MESSAGE_CONSTRAINTS, ()
                -> ParserUtil.parseAreas(List.of(VALID_AREA, INVALID_AREA)));
    }

    @Test
    public void parseArea_repeatedInternalWhitespace_returnsArea() throws Exception {
        assertEquals(new Area("Ang Mo Kio"), ParserUtil.parseArea("ang  mo  kio"));
        assertEquals(new Area("Ang Mo Kio"), ParserUtil.parseArea("  Ang   Mo   Kio  "));
    }

    @Test
    public void parseAreas_repeatedInternalWhitespace_returnsDeduplicatedSet() throws Exception {
        Set<Area> actualAreas = ParserUtil.parseAreas(List.of("Ang Mo Kio", "ang  mo  kio"));
        assertEquals(Set.of(new Area("Ang Mo Kio")), actualAreas);
    }

    @Test
    public void parseHousingType_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseHousingType(null));
    }

    @Test
    public void parseHousingType_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, HousingType.MESSAGE_CONSTRAINTS, ()
                -> ParserUtil.parseHousingType(INVALID_HOUSING_TYPE));
    }

    @Test
    public void parseHousingType_validValue_returnsHousingType() throws Exception {
        assertEquals(HousingType.HDB, ParserUtil.parseHousingType(VALID_HOUSING_TYPE));
        assertEquals(HousingType.HDB, ParserUtil.parseHousingType(WHITESPACE + "HDB" + WHITESPACE));
    }

    @Test
    public void parseHousingTypes_collectionWithValidTypes_returnsDeduplicatedSet() throws Exception {
        Set<HousingType> actualTypes = ParserUtil.parseHousingTypes(List.of("hdb", "HDB", "Condo"));
        Set<HousingType> expectedTypes = Set.of(HousingType.HDB, HousingType.CONDO);

        assertEquals(expectedTypes, actualTypes);
    }

    @Test
    public void parseHousingTypes_collectionWithInvalidType_throwsParseException() {
        assertThrows(ParseException.class, HousingType.MESSAGE_CONSTRAINTS, ()
                -> ParserUtil.parseHousingTypes(List.of("HDB", INVALID_HOUSING_TYPE)));
    }

    @Test
    public void rejectUnknownPrefixes_unknownToken_throwsParseException() {
        String expected = String.format(MESSAGE_UNKNOWN_PREFIX, "bb/") + "\nusage";

        assertThrows(ParseException.class, expected, ()
                -> ParserUtil.rejectUnknownPrefixes(" n/Rachel a/1 Road bb/400000-600000", "usage",
                        PREFIX_NAME, PREFIX_ADDRESS, PREFIX_BUDGET));
    }

    @Test
    public void rejectUnknownPrefixes_uppercaseUnknownToken_throwsParseException() {
        String expected = String.format(MESSAGE_UNKNOWN_PREFIX, "BB/") + "\nusage";

        assertThrows(ParseException.class, expected, ()
                -> ParserUtil.rejectUnknownPrefixes(" n/Rachel BB/400000-600000", "usage",
                        PREFIX_NAME, PREFIX_BUDGET));
    }

    @Test
    public void rejectUnknownPrefixes_tokenBeforeAnyKnownPrefix_doesNotThrow() throws Exception {
        ParserUtil.rejectUnknownPrefixes("bb/400000-600000 n/Rachel", "usage", PREFIX_NAME);
    }

    @Test
    public void rejectUnknownPrefixes_knownPrefixesAndHarmlessSlashes_doesNotThrow() throws Exception {
        ParserUtil.rejectUnknownPrefixes(" n/Rachel a/Blk 5/6, #08-45 t/2br/3ba", "usage",
                PREFIX_NAME, PREFIX_ADDRESS, PREFIX_TAG);
        ParserUtil.rejectUnknownPrefixes(" n/Rachel p/91234567", "usage", PREFIX_NAME, PREFIX_PHONE);
    }

    @Test
    public void rejectUnknownPrefixes_slashInAddressText_doesNotThrow() throws Exception {
        ParserUtil.rejectUnknownPrefixes(" n/Rachel a/12 Main Street c/o Mary Tan", "usage",
                PREFIX_NAME, PREFIX_ADDRESS);
        ParserUtil.rejectUnknownPrefixes(" n/Rachel a/7 Sunset Way w/ lift", "usage",
                PREFIX_NAME, PREFIX_ADDRESS);
        ParserUtil.rejectUnknownPrefixes(" n/Rachel a/9 Grove Road d/o Ramasamy", "usage",
                PREFIX_NAME, PREFIX_ADDRESS);
    }

    @Test
    public void rejectUnknownPrefixes_tokenBeginningLikeAKnownPrefix_throwsParseException() {
        String doubled = String.format(MESSAGE_UNKNOWN_PREFIX, "nn/") + "\nusage";
        assertThrows(ParseException.class, doubled, ()
                -> ParserUtil.rejectUnknownPrefixes(" n/Rachel a/1 Road nn/Someone Else", "usage",
                        PREFIX_NAME, PREFIX_ADDRESS));

        String wrongSecondLetter = String.format(MESSAGE_UNKNOWN_PREFIX, "pa/") + "\nusage";
        assertThrows(ParseException.class, wrongSecondLetter, ()
                -> ParserUtil.rejectUnknownPrefixes(" n/Rachel pa/91234567", "usage",
                        PREFIX_NAME, PREFIX_PHONE));

        String uppercaseDoubled = String.format(MESSAGE_UNKNOWN_PREFIX, "TT/") + "\nusage";
        assertThrows(ParseException.class, uppercaseDoubled, ()
                -> ParserUtil.rejectUnknownPrefixes(" n/Rachel a/1 Road TT/urgent", "usage",
                        PREFIX_NAME, PREFIX_ADDRESS, PREFIX_TAG));
    }

    @Test
    public void rejectUnknownPrefixes_uppercaseKnownPrefix_throwsParseException() {
        Prefix[] allPrefixes = {PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_BUDGET,
            PREFIX_ASKING_PRICE, PREFIX_AREA, PREFIX_HOUSING_TYPE, PREFIX_TAG};
        String[] uppercasePrefixes = {"N/", "P/", "E/", "A/", "B/", "S/", "L/", "H/", "T/"};

        for (String uppercasePrefix : uppercasePrefixes) {
            String expected = String.format(MESSAGE_UNKNOWN_PREFIX, uppercasePrefix) + "\nusage";
            assertThrows(ParseException.class, expected, ()
                    -> ParserUtil.rejectUnknownPrefixes(" n/Bob a/1 Road " + uppercasePrefix + "400000", "usage",
                            allPrefixes));
        }
    }

    @Test
    public void rejectUnknownPrefixes_prefixOfAnotherCommand_throwsParseException() {
        String expected = String.format(MESSAGE_UNKNOWN_PREFIX, "r/") + "\nusage";

        assertThrows(ParseException.class, expected, ()
                -> ParserUtil.rejectUnknownPrefixes(" n/Bob a/1 Road r/buyer", "usage",
                        PREFIX_NAME, PREFIX_ADDRESS));
    }

    @Test
    public void rejectUnknownPrefixes_addressTextBeginningLikeAPrefix_throwsParseException() {
        // Limit of the rule: Tel/ and nr/ begin like a tag and a name prefix, so they are reported, not kept as text.
        String telephone = String.format(MESSAGE_UNKNOWN_PREFIX, "Tel/") + "\nusage";
        assertThrows(ParseException.class, telephone, ()
                -> ParserUtil.rejectUnknownPrefixes(" n/Bob a/1 Test Road Tel/Fax 6", "usage",
                        PREFIX_NAME, PREFIX_ADDRESS));

        String near = String.format(MESSAGE_UNKNOWN_PREFIX, "nr/") + "\nusage";
        assertThrows(ParseException.class, near, ()
                -> ParserUtil.rejectUnknownPrefixes(" n/Bob a/1 Test Road nr/ MRT", "usage",
                        PREFIX_NAME, PREFIX_ADDRESS));
    }

    @Test
    public void rejectUnknownPrefixes_freeTextAbbreviationInAnyCase_doesNotThrow() throws Exception {
        ParserUtil.rejectUnknownPrefixes(" n/Bob a/12 Main Street s/o Ravi", "usage", PREFIX_NAME, PREFIX_ADDRESS);
        ParserUtil.rejectUnknownPrefixes(" n/Bob a/12 Main Street S/o Ravi", "usage", PREFIX_NAME, PREFIX_ADDRESS);
        ParserUtil.rejectUnknownPrefixes(" n/Bob a/12 Main Street b/c Tan", "usage", PREFIX_NAME, PREFIX_ADDRESS);
        ParserUtil.rejectUnknownPrefixes(" n/Bob a/12 Main Street B/c Tan", "usage", PREFIX_NAME, PREFIX_ADDRESS);
    }
}
