package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.RoleCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ClientRole;

/**
 * Parses input arguments and creates a new {@code RoleCommand} object.
 */
public class RoleCommandParser implements Parser<RoleCommand> {

    @Override
    public RoleCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        String[] arguments = trimmedArgs.isEmpty() ? new String[0] : trimmedArgs.split("\\s+");

        if (arguments.length != 2) {
            throw new ParseException(RoleCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        }

        Index index = parseIndex(arguments[0]);
        if (arguments[1].equalsIgnoreCase("none")) {
            return new RoleCommand(index, null);
        }

        if (!ClientRole.isValidRole(arguments[1])) {
            throw new ParseException(RoleCommand.MESSAGE_INVALID_ROLE);
        }

        return new RoleCommand(index, ClientRole.fromString(arguments[1]));
    }

    private Index parseIndex(String indexText) throws ParseException {
        try {
            return ParserUtil.parseIndex(indexText);
        } catch (ParseException pe) {
            throw new ParseException(RoleCommand.MESSAGE_INVALID_COMMAND_FORMAT, pe);
        }
    }
}
