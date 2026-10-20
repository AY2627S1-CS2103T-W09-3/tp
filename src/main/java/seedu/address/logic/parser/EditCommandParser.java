package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_AREA;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ASKING_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BUDGET;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_HOUSING_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Area;
import seedu.address.model.person.Budget;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.Price;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new EditCommand object
 */
public class EditCommandParser implements Parser<EditCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the EditCommand
     * and returns an EditCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                        PREFIX_BUDGET, PREFIX_ASKING_PRICE, PREFIX_AREA, PREFIX_HOUSING_TYPE, PREFIX_TAG);

        String indexArgument = argMultimap.getPreamble().trim();
        Index index;

        try {
            index = ParserUtil.parseIndex(indexArgument);
        } catch (ParseException pe) {
            String message = indexArgument.matches("-?[0-9]+")
                    ? Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX
                    : String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);
            throw new ParseException(message, pe);
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                PREFIX_BUDGET, PREFIX_ASKING_PRICE);

        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();

        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            editPersonDescriptor.setName(ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get()));
        }
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            editPersonDescriptor.setPhone(ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get()));
        }
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            editPersonDescriptor.setEmail(ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get()));
        }
        if (argMultimap.getValue(PREFIX_ADDRESS).isPresent()) {
            editPersonDescriptor.setAddress(ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get()));
        }
        if (argMultimap.getValue(PREFIX_BUDGET).isPresent()) {
            editPersonDescriptor.setBudget(parseBudgetForEdit(argMultimap.getValue(PREFIX_BUDGET).get()));
        }
        if (argMultimap.getValue(PREFIX_ASKING_PRICE).isPresent()) {
            editPersonDescriptor.setAskingPrice(
                    parseAskingPriceForEdit(argMultimap.getValue(PREFIX_ASKING_PRICE).get()));
        }
        parseAreasForEdit(argMultimap.getAllValues(PREFIX_AREA)).ifPresent(editPersonDescriptor::setAreas);
        parseHousingTypesForEdit(argMultimap.getAllValues(PREFIX_HOUSING_TYPE))
                .ifPresent(editPersonDescriptor::setHousingTypes);
        parseTagsForEdit(argMultimap.getAllValues(PREFIX_TAG)).ifPresent(editPersonDescriptor::setTags);

        if (!editPersonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCommand(index, editPersonDescriptor);
    }

    /**
     * Parses a budget for an edit, treating an empty value as a request to clear the field.
     */
    private Budget parseBudgetForEdit(String budget) throws ParseException {
        return budget.trim().isEmpty() ? null : ParserUtil.parseBudget(budget);
    }

    /**
     * Parses an asking price for an edit, treating an empty value as a request to clear the field.
     */
    private Price parseAskingPriceForEdit(String askingPrice) throws ParseException {
        return askingPrice.trim().isEmpty() ? null : ParserUtil.parsePrice(askingPrice);
    }

    /**
     * Parses {@code Collection<String> areas} into a {@code Set<Area>} if {@code areas} is non-empty.
     * If {@code areas} contains only one element which is an empty string, it will be parsed into a
     * {@code Set<Area>} containing zero areas.
     */
    private Optional<Set<Area>> parseAreasForEdit(Collection<String> areas) throws ParseException {
        requireNonNull(areas);

        if (areas.isEmpty()) {
            return Optional.empty();
        }
        Collection<String> areaSet = areas.size() == 1 && areas.contains("") ? Collections.emptySet() : areas;
        return Optional.of(ParserUtil.parseAreas(areaSet));
    }

    /**
     * Parses {@code Collection<String> housingTypes} into a {@code Set<HousingType>} if
     * {@code housingTypes} is non-empty. If {@code housingTypes} contains only one element which is an
     * empty string, it will be parsed into a {@code Set<HousingType>} containing zero housing types.
     */
    private Optional<Set<HousingType>> parseHousingTypesForEdit(Collection<String> housingTypes)
            throws ParseException {
        requireNonNull(housingTypes);

        if (housingTypes.isEmpty()) {
            return Optional.empty();
        }
        Collection<String> housingTypeSet = housingTypes.size() == 1 && housingTypes.contains("")
                ? Collections.emptySet()
                : housingTypes;
        return Optional.of(ParserUtil.parseHousingTypes(housingTypeSet));
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>} if {@code tags} is non-empty.
     * If {@code tags} contains only one element which is an empty string, it will be parsed into a
     * {@code Set<Tag>} containing zero tags.
     */
    private Optional<Set<Tag>> parseTagsForEdit(Collection<String> tags) throws ParseException {
        requireNonNull(tags);

        if (tags.isEmpty()) {
            return Optional.empty();
        }
        Collection<String> tagSet = tags.size() == 1 && tags.contains("") ? Collections.emptySet() : tags;
        return Optional.of(ParserUtil.parseTags(tagSet));
    }

}
