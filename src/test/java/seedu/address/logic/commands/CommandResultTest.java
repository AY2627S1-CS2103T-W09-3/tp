package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;

public class CommandResultTest {
    @Test
    public void constructor_existingForms_preserveFlagsWithoutViewIndex() {
        CommandResult feedbackResult = new CommandResult("feedback");
        assertEquals("feedback", feedbackResult.getFeedbackToUser());
        assertFalse(feedbackResult.isShowHelp());
        assertFalse(feedbackResult.isExit());
        assertFalse(feedbackResult.isAwaitingConfirmation());
        assertFalse(feedbackResult.shouldPreserveSelection());
        assertTrue(feedbackResult.getViewIndex().isEmpty());

        CommandResult helpResult = new CommandResult("help", true, false);
        assertTrue(helpResult.isShowHelp());
        assertFalse(helpResult.isExit());
        assertTrue(helpResult.getViewIndex().isEmpty());

        CommandResult exitResult = new CommandResult("exit", false, true);
        assertFalse(exitResult.isShowHelp());
        assertTrue(exitResult.isExit());
        assertTrue(exitResult.getViewIndex().isEmpty());

        CommandResult pendingResult = new CommandResult("Confirm clear", false, false, true);
        assertTrue(pendingResult.isAwaitingConfirmation());
        assertFalse(pendingResult.isShowHelp());
        assertFalse(pendingResult.isExit());
        assertTrue(pendingResult.getViewIndex().isEmpty());
    }

    @Test
    public void constructor_viewIndex_returnsIndexWithoutOtherActions() {
        CommandResult result = new CommandResult("Showing Alice.", Index.fromOneBased(2));

        assertEquals("Showing Alice.", result.getFeedbackToUser());
        assertEquals(Optional.of(Index.fromOneBased(2)), result.getViewIndex());
        assertFalse(result.isAwaitingConfirmation());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
        assertFalse(result.shouldPreserveSelection());
    }

    @Test
    public void preservingSelection_keepsSelectionWithoutViewIndex() {
        CommandResult result = CommandResult.preservingSelection("history");

        assertTrue(result.shouldPreserveSelection());
        assertTrue(result.getViewIndex().isEmpty());
        assertFalse(result.isShowHelp());
        assertFalse(result.isAwaitingConfirmation());
        assertNotEquals(new CommandResult("history"), result);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CommandResult(null));
        assertThrows(NullPointerException.class, () -> new CommandResult(null, false, false));
        assertThrows(NullPointerException.class, () -> new CommandResult(null, false, false, true));
        assertThrows(NullPointerException.class, () -> new CommandResult(null, Index.fromOneBased(1)));
        assertThrows(NullPointerException.class, () -> new CommandResult("feedback", (Index) null));
    }

    @Test
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false)));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false, false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different showHelp value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true, false)));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, true)));

        // different awaitingConfirmation value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, false, true)));

        // different preserveSelection value -> returns false
        assertFalse(commandResult.equals(CommandResult.preservingSelection("feedback")));
    }

    @Test
    public void equals_viewIndexes_comparesPresenceAndValue() {
        CommandResult viewResult = new CommandResult("feedback", Index.fromOneBased(1));
        CommandResult plainResult = new CommandResult("feedback");

        assertEquals(viewResult, new CommandResult("feedback", Index.fromOneBased(1)));
        assertNotEquals(viewResult, new CommandResult("feedback", Index.fromOneBased(2)));
        assertNotEquals(viewResult, new CommandResult("different", Index.fromOneBased(1)));
        assertNotEquals(viewResult, plainResult);
        assertNotEquals(plainResult, viewResult);
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different showHelp value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true, false).hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, true).hashCode());

        // different awaitingConfirmation value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, false, true).hashCode());

        // different preserveSelection value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), CommandResult.preservingSelection("feedback").hashCode());
    }

    @Test
    public void hashCode_equalViewIndexes_sameHashCode() {
        CommandResult firstResult = new CommandResult("feedback", Index.fromOneBased(2));
        CommandResult secondResult = new CommandResult("feedback", Index.fromOneBased(2));

        assertEquals(firstResult, secondResult);
        assertEquals(firstResult.hashCode(), secondResult.hashCode());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit() + ", viewIndex=null"
                + ", awaitingConfirmation=" + commandResult.isAwaitingConfirmation()
                + ", preserveSelection=" + commandResult.shouldPreserveSelection() + "}";
        assertEquals(expected, commandResult.toString());
    }

    @Test
    public void toStringMethod_viewResult_includesIndex() {
        Index viewIndex = Index.fromOneBased(2);
        CommandResult commandResult = new CommandResult("feedback", viewIndex);
        String expected = CommandResult.class.getCanonicalName()
                + "{feedbackToUser=feedback, showHelp=false, exit=false, viewIndex=" + viewIndex
                + ", awaitingConfirmation=false, preserveSelection=false}";
        assertEquals(expected, commandResult.toString());
    }
}
