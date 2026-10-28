package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Price;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests name support through command parsing, the real model, history and JSON storage.
 */
public class ClientNameIntegrationTest {

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
    public void execute_commonNameForms_addsAndReloadsClients() throws Exception {
        List<String> names = List.of("Kumar s/o Ravi", "Anne-Marie Tan", "José Tan", "D’Arcy O'Neil",
                "Arun a/l Mani", "Devi a/p Mani", "Devi d/o Ravi", "陈伟");
        for (int i = 0; i < names.size(); i++) {
            addClient(names.get(i), i + 1);
            assertEquals(new Name(names.get(i)), model.getFilteredPersonList().get(i).getName());
        }

        assertEquals(names.size(), model.getFilteredPersonList().size());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_editNameWithRequirements_supportsUndoRedoAndReload() throws Exception {
        addClient("Original Client", 1);

        logic.execute("edit 1 n/Kumar s/o Ravi s/950000 a/4 Test Road");

        Person edited = model.getFilteredPersonList().getFirst();
        assertEquals(new Name("Kumar s/o Ravi"), edited.getName());
        assertEquals(new Price("950000"), edited.getAskingPrice());
        assertEquals("4 Test Road", edited.getAddress().value);
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());

        logic.execute("undo");
        assertEquals(new Name("Original Client"), model.getFilteredPersonList().getFirst().getName());
        assertNull(model.getFilteredPersonList().getFirst().getAskingPrice());

        logic.execute("redo");
        assertEquals(edited, model.getFilteredPersonList().getFirst());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_requirementsBeforeName_preservesRelationshipMarker() throws Exception {
        logic.execute("add s/950000 a/3 Test Road n/Arun a/l Mani p/90010103 e/arun@example.com");

        Person client = model.getFilteredPersonList().getFirst();
        assertEquals(new Name("Arun a/l Mani"), client.getName());
        assertEquals(new Price("950000"), client.getAskingPrice());
        assertEquals("3 Test Road", client.getAddress().value);
    }

    @Test
    public void execute_canonicallyEquivalentName_rejectsDuplicate() throws Exception {
        addClient("José Tan", 1);
        AddressBookSnapshot before = model.captureSnapshot();

        assertThrows(CommandException.class, Messages.MESSAGE_DUPLICATE_NAME, () -> addClient("Jose\u0301 Tan", 2));

        assertEquals(before, model.captureSnapshot());
        assertEquals(1, model.getUndoHistory().size());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_findEquivalentAccentSpellings_findsSavedClient() throws Exception {
        addClient("Jose\u0301 Tan", 1);

        logic.execute("find JOSE\u0301");
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(new Name("José Tan"), model.getFilteredPersonList().getFirst().getName());

        logic.execute("find José");
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidName_reportsNameErrorWithoutChangingData() throws Exception {
        addClient("Original Client", 1);
        AddressBookSnapshot before = model.captureSnapshot();
        String saved = Files.readString(addressBookStorage.getAddressBookFilePath());

        assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, () -> addClient("Kumar s/o Ravi*", 2));
        assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, () -> {
            logic.execute("edit 1 n/Kumar s/o Ravi*");
        });

        assertEquals(before, model.captureSnapshot());
        assertEquals(1, model.getUndoHistory().size());
        assertEquals(saved, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }

    @Test
    public void execute_invalidAskingPriceOutsideName_reportsPriceError() {
        assertThrows(ParseException.class, Price.MESSAGE_CONSTRAINTS, () -> {
            logic.execute("add n/Valid Client s/office p/90010103 e/client@example.com a/3 Test Road");
        });
        assertThrows(ParseException.class, Price.MESSAGE_CONSTRAINTS, () -> {
            logic.execute("add n/Valid Client p/90010103 e/client@example.com a/3 Test Road s/o");
        });

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertTrue(model.getUndoHistory().isEmpty());
    }

    private void addClient(String name, int clientNumber) throws Exception {
        logic.execute("add n/" + name + " p/" + (90010100 + clientNumber)
                + " e/client" + clientNumber + "@example.com a/3 Test Road");
    }
}
