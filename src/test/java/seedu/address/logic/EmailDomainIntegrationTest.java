package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.history.AddressBookSnapshot;
import seedu.address.model.person.Email;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests domain-hyphen validation through commands, history and real JSON storage.
 */
public class EmailDomainIntegrationTest {

    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private Logic logic;
    private JsonAddressBookStorage addressBookStorage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_addDomainHyphens_savesAndReloads() throws Exception {
        List<String> emails = List.of("client@foo--bar.com", "client@xn--bcher-kva.de",
                "client@example.xn--p1ai", "client@a--b");
        for (int i = 0; i < emails.size(); i++) {
            addClient(emails.get(i), i + 1);
            assertEquals(emails.get(i), model.getFilteredPersonList().get(i).getEmail().value);
        }

        assertEquals(emails.size(), model.getFilteredPersonList().size());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_editDomainHyphens_supportsUndoRedoAndReload() throws Exception {
        addClient("client@example.com", 1);

        logic.execute("edit 1 e/client@xn--bcher-kva.de");
        assertSavedEmail("client@xn--bcher-kva.de");

        logic.execute("undo");
        assertSavedEmail("client@example.com");

        logic.execute("redo");
        assertSavedEmail("client@xn--bcher-kva.de");
    }

    @Test
    public void execute_duplicateEmailIgnoringCase_rejectsWithoutChangingData() throws Exception {
        addClient("client@foo--bar.com", 1);
        addClient("other@example.com", 2);
        AddressBookSnapshot before = model.captureSnapshot();
        String saved = Files.readString(addressBookStorage.getAddressBookFilePath());
        String expected = String.format(Messages.MESSAGE_DUPLICATE_EMAIL, "Domain Client 1");

        assertThrows(CommandException.class, expected, () -> addClient("CLIENT@FOO--BAR.COM", 3));
        assertThrows(CommandException.class, expected, () -> logic.execute("edit 2 e/CLIENT@FOO--BAR.COM"));

        assertEquals(before, model.captureSnapshot());
        assertEquals(2, model.getUndoHistory().size());
        assertEquals(saved, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }

    @Test
    public void execute_invalidEmail_rejectsWithoutChangingData() throws Exception {
        addClient("client@example.com", 1);
        AddressBookSnapshot before = model.captureSnapshot();
        String saved = Files.readString(addressBookStorage.getAddressBookFilePath());

        for (String email : List.of("client@-foo--bar.com", "client@foo--bar-.com", "client@foo..com",
                "client@foo--bar.c", "client..name@foo--bar.com", "client--name@foo--bar.com")) {
            assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, () -> addClient(email, 2));
            assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, () -> logic.execute("edit 1 e/" + email));
        }

        assertEquals(before, model.captureSnapshot());
        assertEquals(1, model.getUndoHistory().size());
        assertEquals(saved, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }

    private void addClient(String email, int clientNumber) throws Exception {
        logic.execute("add n/Domain Client " + clientNumber + " p/" + (90020400 + clientNumber)
                + " e/" + email + " a/1 Test Road");
    }

    private void assertSavedEmail(String expected) throws Exception {
        assertEquals(expected, model.getFilteredPersonList().getFirst().getEmail().value);
        assertEquals(expected, addressBookStorage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getEmail().value);
    }
}
