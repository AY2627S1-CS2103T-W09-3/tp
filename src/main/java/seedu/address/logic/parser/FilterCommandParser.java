package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_AREA;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ASKING_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BUDGET;
import static seedu.address.logic.parser.CliSyntax.PREFIX_HOUSING_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Set;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Area;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.PersonMatchesFilterPredicate;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new FilterCommand object
 */
public class FilterCommandParser implements Parser<FilterCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FilterCommand
     * and returns a FilterCommand object for execution.
     * The role, the tags, the areas, the housing types, the budget and the asking price are all optional, so no
     * argument filters nothing out. The budget and the asking price are each a price range written as
     * {@code MIN-MAX}, or a single price.
     *
     * @throws ParseException if the arguments have text before the prefixes, an invalid role, tag, area, housing
     *     type, budget or asking price, or more than one role, budget or asking price.
     */
    public FilterCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_ROLE, PREFIX_TAG, PREFIX_AREA,
                PREFIX_HOUSING_TYPE, PREFIX_BUDGET, PREFIX_ASKING_PRICE);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_ROLE, PREFIX_BUDGET, PREFIX_ASKING_PRICE);

        PersonMatchesFilterPredicate.Builder predicateBuilder = new PersonMatchesFilterPredicate.Builder();
        if (argMultimap.getValue(PREFIX_ROLE).isPresent()) {
            predicateBuilder.withRole(parseRole(argMultimap.getValue(PREFIX_ROLE).get()));
        }
        Set<Tag> tags = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));
        predicateBuilder.withTags(tags);
        Set<Area> areas = ParserUtil.parseAreas(argMultimap.getAllValues(PREFIX_AREA));
        predicateBuilder.withAreas(areas);
        Set<HousingType> housingTypes = ParserUtil.parseHousingTypes(argMultimap.getAllValues(PREFIX_HOUSING_TYPE));
        predicateBuilder.withHousingTypes(housingTypes);
        if (argMultimap.getValue(PREFIX_BUDGET).isPresent()) {
            predicateBuilder.withBudget(ParserUtil.parsePriceRange(argMultimap.getValue(PREFIX_BUDGET).get()));
        }
        if (argMultimap.getValue(PREFIX_ASKING_PRICE).isPresent()) {
            predicateBuilder.withAskingPrice(
                    ParserUtil.parsePriceRange(argMultimap.getValue(PREFIX_ASKING_PRICE).get()));
        }

        return new FilterCommand(predicateBuilder.build());
    }

    /**
     * Parses {@code role} into a {@code ClientRole}, ignoring case and surrounding whitespace.
     *
     * @throws ParseException if {@code role} is not buyer, seller or both.
     */
    private static ClientRole parseRole(String role) throws ParseException {
        String trimmedRole = role.trim();
        if (!ClientRole.isValidRole(trimmedRole)) {
            throw new ParseException(ClientRole.MESSAGE_CONSTRAINTS);
        }
        return ClientRole.fromString(trimmedRole);
    }
}
