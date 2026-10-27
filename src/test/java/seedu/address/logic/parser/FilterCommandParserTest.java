package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ASKING_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BUDGET;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.model.person.Area;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.PersonMatchesFilterPredicate;
import seedu.address.model.person.Price;
import seedu.address.model.person.PriceRange;
import seedu.address.model.tag.Tag;

public class FilterCommandParserTest {

    private static final String INVALID_FORMAT_MESSAGE =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE);

    private FilterCommandParser parser = new FilterCommandParser();

    private static FilterCommand filterOf(Optional<ClientRole> role, String... tagNames) {
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            tags.add(new Tag(tagName));
        }
        PersonMatchesFilterPredicate.Builder builder = new PersonMatchesFilterPredicate.Builder().withTags(tags);
        role.ifPresent(builder::withRole);
        return new FilterCommand(builder.build());
    }

    private static FilterCommand filterOfAreas(Optional<ClientRole> role, String... areaNames) {
        Set<Area> areas = new HashSet<>();
        for (String areaName : areaNames) {
            areas.add(new Area(areaName));
        }
        PersonMatchesFilterPredicate.Builder builder = new PersonMatchesFilterPredicate.Builder().withAreas(areas);
        role.ifPresent(builder::withRole);
        return new FilterCommand(builder.build());
    }

    @Test
    public void parse_noArguments_returnsFilterWithNoCriteria() {
        assertParseSuccess(parser, "", filterOf(Optional.empty()));
        assertParseSuccess(parser, "   \t ", filterOf(Optional.empty()));
    }

    @Test
    public void parse_roleOnly_returnsFilterWithRole() {
        assertParseSuccess(parser, " r/buyer", filterOf(Optional.of(ClientRole.BUYER)));
        assertParseSuccess(parser, " r/seller", filterOf(Optional.of(ClientRole.SELLER)));
        assertParseSuccess(parser, " r/both", filterOf(Optional.of(ClientRole.BOTH)));
    }

    @Test
    public void parse_roleIgnoresCaseAndWhitespace_returnsFilterWithRole() {
        assertParseSuccess(parser, " r/BuYeR", filterOf(Optional.of(ClientRole.BUYER)));
        assertParseSuccess(parser, "   r/  seller   ", filterOf(Optional.of(ClientRole.SELLER)));
    }

    @Test
    public void parse_tagsOnly_returnsFilterWithTags() {
        assertParseSuccess(parser, " t/urgent", filterOf(Optional.empty(), "urgent"));
        assertParseSuccess(parser, " t/urgent t/weekend", filterOf(Optional.empty(), "urgent", "weekend"));
        assertParseSuccess(parser, " t/UrGent", filterOf(Optional.empty(), "UrGent"));
    }

    @Test
    public void parse_roleAndTags_returnsFilterWithBothInAnyOrder() {
        FilterCommand expected = filterOf(Optional.of(ClientRole.BUYER), "urgent");

        assertParseSuccess(parser, " r/buyer t/urgent", expected);
        assertParseSuccess(parser, " t/urgent r/buyer", expected);
    }

    @Test
    public void parse_areasOnly_returnsFilterWithAreas() {
        assertParseSuccess(parser, " l/Bedok", filterOfAreas(Optional.empty(), "Bedok"));
        assertParseSuccess(parser, " l/Bedok l/Tampines", filterOfAreas(Optional.empty(), "Bedok", "Tampines"));
        assertParseSuccess(parser, " l/toa payoh", filterOfAreas(Optional.empty(), "Toa Payoh"));
        assertParseSuccess(parser, " l/  BEDOK  ", filterOfAreas(Optional.empty(), "Bedok"));
    }

    @Test
    public void parse_housingTypesOnly_returnsFilterWithHousingTypes() {
        FilterCommand hdbOnly = new FilterCommand(new PersonMatchesFilterPredicate.Builder()
                .withHousingTypes(Set.of(HousingType.HDB)).build());
        FilterCommand hdbAndCondo = new FilterCommand(new PersonMatchesFilterPredicate.Builder()
                .withHousingTypes(Set.of(HousingType.HDB, HousingType.CONDO)).build());

        assertParseSuccess(parser, " h/HDB", hdbOnly);
        assertParseSuccess(parser, " h/  hdb  ", hdbOnly);
        assertParseSuccess(parser, " h/HDB h/Condo", hdbAndCondo);
        assertParseSuccess(parser, " h/condo h/hdb h/HDB", hdbAndCondo);
    }

    @Test
    public void parse_budget_returnsFilterWithPriceRange() {
        FilterCommand range = new FilterCommand(new PersonMatchesFilterPredicate.Builder()
                .withBudget(new PriceRange(new Price("500000"), new Price("600000"))).build());
        FilterCommand singlePrice = new FilterCommand(new PersonMatchesFilterPredicate.Builder()
                .withBudget(new PriceRange(new Price("850000"))).build());

        assertParseSuccess(parser, " b/500000-600000", range);
        assertParseSuccess(parser, " b/500,000-600,000", range);
        assertParseSuccess(parser, " b/  500000 - 600000  ", range);
        assertParseSuccess(parser, " b/850000", singlePrice);
        assertParseSuccess(parser, " b/850,000", singlePrice);
    }

    @Test
    public void parse_budgetWithOtherCriteria_returnsFilterWithAllInAnyOrder() {
        FilterCommand expected = new FilterCommand(new PersonMatchesFilterPredicate.Builder()
                .withRole(ClientRole.BUYER).withAreas(Set.of(new Area("Bedok")))
                .withBudget(new PriceRange(new Price("500000"), new Price("600000"))).build());

        assertParseSuccess(parser, " r/buyer l/Bedok b/500000-600000", expected);
        assertParseSuccess(parser, " b/500000-600000 l/Bedok r/buyer", expected);
    }

    @Test
    public void parse_invalidBudget_throwsParseException() {
        for (String input : List.of(" b/", " b/abc", " b/0", " b/500000-", " b/-600000", " b/1-2-3", " r/buyer b/")) {
            assertParseFailure(parser, input, Price.MESSAGE_CONSTRAINTS);
        }
        assertParseFailure(parser, " b/600000-500000", PriceRange.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedBudget_throwsParseException() {
        assertParseFailure(parser, " b/1-2 b/3-4", getErrorMessageForDuplicatePrefixes(PREFIX_BUDGET));
        assertParseFailure(parser, " b/100 l/Bedok b/200", getErrorMessageForDuplicatePrefixes(PREFIX_BUDGET));
    }

    @Test
    public void parse_askingPrice_returnsFilterWithPriceRange() {
        FilterCommand range = new FilterCommand(new PersonMatchesFilterPredicate.Builder()
                .withAskingPrice(new PriceRange(new Price("800000"), new Price("900000"))).build());
        FilterCommand singlePrice = new FilterCommand(new PersonMatchesFilterPredicate.Builder()
                .withAskingPrice(new PriceRange(new Price("850000"))).build());

        assertParseSuccess(parser, " s/800000-900000", range);
        assertParseSuccess(parser, " s/800,000 - 900,000", range);
        assertParseSuccess(parser, " s/850000", singlePrice);
        assertParseSuccess(parser, " s/  850,000  ", singlePrice);
    }

    @Test
    public void parse_budgetAndAskingPrice_areKeptApart() {
        FilterCommand expected = new FilterCommand(new PersonMatchesFilterPredicate.Builder()
                .withBudget(new PriceRange(new Price("500000"), new Price("600000")))
                .withAskingPrice(new PriceRange(new Price("800000"), new Price("900000"))).build());

        assertParseSuccess(parser, " b/500000-600000 s/800000-900000", expected);
        assertParseSuccess(parser, " s/800000-900000 b/500000-600000", expected);
    }

    @Test
    public void parse_invalidAskingPrice_throwsParseException() {
        for (String input : List.of(" s/", " s/abc", " s/0", " s/800000-", " s/-900000", " s/1-2-3", " r/seller s/")) {
            assertParseFailure(parser, input, Price.MESSAGE_CONSTRAINTS);
        }
        assertParseFailure(parser, " s/900000-800000", PriceRange.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedAskingPrice_throwsParseException() {
        assertParseFailure(parser, " s/1-2 s/3-4", getErrorMessageForDuplicatePrefixes(PREFIX_ASKING_PRICE));
        assertParseFailure(parser, " s/100 b/100 s/200", getErrorMessageForDuplicatePrefixes(PREFIX_ASKING_PRICE));
    }

    @Test
    public void parse_roleTagsAreasAndHousingTypes_returnsFilterWithAllInAnyOrder() {
        Set<Tag> tags = Set.of(new Tag("urgent"));
        Set<Area> areas = Set.of(new Area("Bedok"));
        FilterCommand expected = new FilterCommand(new PersonMatchesFilterPredicate.Builder()
                .withRole(ClientRole.BUYER).withTags(tags).withAreas(areas)
                .withHousingTypes(Set.of(HousingType.HDB)).build());

        assertParseSuccess(parser, " r/buyer t/urgent l/Bedok h/HDB", expected);
        assertParseSuccess(parser, " h/HDB l/Bedok t/urgent r/buyer", expected);
    }

    @Test
    public void parse_invalidHousingType_throwsParseException() {
        for (String input : List.of(" h/", " h/Shophouse", " h/HDB h/", " h/Condominium", " r/buyer h/Flat")) {
            assertParseFailure(parser, input, HousingType.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidArea_throwsParseException() {
        for (String input : List.of(" l/", " l/Narnia", " l/Bedok l/", " l/Toa  Payoh", " r/buyer l/Bishan East")) {
            assertParseFailure(parser, input, Area.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidRole_throwsParseException() {
        for (String input : List.of(" r/landlord", " r/", " r/   ", " r/buyer seller")) {
            assertParseFailure(parser, input, ClientRole.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_repeatedRole_throwsParseException() {
        assertParseFailure(parser, " r/buyer r/seller", getErrorMessageForDuplicatePrefixes(PREFIX_ROLE));
        assertParseFailure(parser, " r/buyer t/urgent r/seller", getErrorMessageForDuplicatePrefixes(PREFIX_ROLE));
    }

    @Test
    public void parse_invalidTag_throwsParseException() {
        for (String input : List.of(" t/", " t/urgent t/", " t/two words", " t/urgent!", " r/buyer t/")) {
            assertParseFailure(parser, input, Tag.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_textBeforePrefixesOrUnknownPrefix_throwsParseException() {
        for (String input : List.of(" buyer", " 1 r/buyer", " n/John", " n/John r/buyer", " p/91234567")) {
            assertParseFailure(parser, input, INVALID_FORMAT_MESSAGE);
        }
    }
}
