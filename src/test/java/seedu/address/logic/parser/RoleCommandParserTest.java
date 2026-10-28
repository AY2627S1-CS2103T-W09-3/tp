package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RoleCommand;
import seedu.address.model.person.ClientRole;

public class RoleCommandParserTest {

    private final RoleCommandParser parser = new RoleCommandParser();

    @Test
    public void parse_validArguments_success() {
        // Equivalence partitions: all supported roles, with case and surrounding whitespace variations.
        assertParseSuccess(parser, " 1 buyer ",
                new RoleCommand(INDEX_FIRST_PERSON, ClientRole.BUYER));
        assertParseSuccess(parser, " 1 SELLER ",
                new RoleCommand(INDEX_FIRST_PERSON, ClientRole.SELLER));
        assertParseSuccess(parser, " 1 Both ",
                new RoleCommand(INDEX_FIRST_PERSON, ClientRole.BOTH));
    }

    @Test
    public void parse_invalidFormat_failure() {
        String[] invalidInputs = {
            // Missing required fields.
            "", "buyer",
            // Index boundaries: zero and negative values, then a value beyond the integer range.
            "0 buyer", "-1 buyer", "2147483648 buyer",
            // Malformed index and surplus argument partitions.
            "1.5 buyer", "abc buyer", "1 buyer extra"
        };

        for (String input : invalidInputs) {
            assertParseFailure(parser, input, RoleCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        }
    }

    @Test
    public void parse_blankOrInvalidRole_failure() {
        // Equivalence partitions: missing role, unknown role, and a combined role token.
        String[] invalidInputs = {"1", "1 agent", "1 buyer-seller"};

        for (String input : invalidInputs) {
            assertParseFailure(parser, input, ClientRole.MESSAGE_CONSTRAINTS);
        }
    }
}
