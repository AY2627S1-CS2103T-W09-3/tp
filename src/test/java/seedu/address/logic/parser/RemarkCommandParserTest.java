package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {

    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_validRemark_success() {
        assertParseSuccess(parser, " 1 r/ Likes swimming.  ",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes swimming.")));
    }

    @Test
    public void parse_emptyRemark_success() {
        assertParseSuccess(parser, " 1 r/", new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")));
        assertParseSuccess(parser, " 1 r/   ", new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")));
    }

    @Test
    public void parse_invalidArguments_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);
        String[] invalidInputs = {
            "", " 1", " 1 Likes swimming.", " r/Hello", " 0 r/Hello", " -1 r/Hello",
            " 1.5 r/Hello", " abc r/Hello", " 2147483648 r/Hello", " 1 extra r/Hello"
        };
        for (String input : invalidInputs) {
            assertParseFailure(parser, input, expectedMessage);
        }
    }

    @Test
    public void parse_duplicateRemark_failure() {
        assertParseFailure(parser, " 1 r/First r/Second",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_REMARK));
    }
}
