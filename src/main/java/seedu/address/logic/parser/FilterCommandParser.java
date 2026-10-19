package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Optional;
import java.util.Set;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.PersonMatchesFilterPredicate;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new FilterCommand object
 */
public class FilterCommandParser implements Parser<FilterCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FilterCommand
     * and returns a FilterCommand object for execution.
     * Both the role and the tags are optional, so no argument filters nothing out.
     *
     * @throws ParseException if the arguments have text before the prefixes, an invalid role or tag,
     *     or more than one role.
     */
    public FilterCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_ROLE, PREFIX_TAG);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_ROLE);

        Optional<ClientRole> role = Optional.empty();
        if (argMultimap.getValue(PREFIX_ROLE).isPresent()) {
            role = Optional.of(parseRole(argMultimap.getValue(PREFIX_ROLE).get()));
        }
        Set<Tag> tags = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));

        return new FilterCommand(new PersonMatchesFilterPredicate(role, tags));
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
