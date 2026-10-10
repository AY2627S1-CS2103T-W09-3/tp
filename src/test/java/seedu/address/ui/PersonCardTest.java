package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    @BeforeAll
    public static void setUpJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException e) {
            // Another UI test has already started JavaFX.
        }
    }

    @Test
    public void constructor_rolePresent_displaysRoleBadge() throws ExecutionException, InterruptedException {
        Person person = new PersonBuilder().withRole(ClientRole.BOTH).withTags("friend").build();

        Label roleBadge = createPersonCard(person);

        assertEquals(ClientRole.BOTH.getDisplayValue(), roleBadge.getText());
        assertTrue(roleBadge.getStyleClass().contains("role-both"));
        assertTrue(roleBadge.isVisible());
        assertTrue(roleBadge.isManaged());
    }

    @Test
    public void constructor_roleAbsent_hidesAndUnmanagesRoleBadge()
            throws ExecutionException, InterruptedException {
        Person person = new PersonBuilder().build();

        Label roleBadge = createPersonCard(person);

        assertFalse(roleBadge.isVisible());
        assertFalse(roleBadge.isManaged());
    }

    @Test
    public void getRoleStyleClass_eachRole_returnsColourClass() {
        assertEquals("role-buyer", PersonCard.getRoleStyleClass(ClientRole.BUYER));
        assertEquals("role-seller", PersonCard.getRoleStyleClass(ClientRole.SELLER));
        assertEquals("role-both", PersonCard.getRoleStyleClass(ClientRole.BOTH));
    }

    private static Label createPersonCard(Person person)
            throws ExecutionException, InterruptedException {
        FutureTask<Label> createCard = new FutureTask<>(() -> {
            PersonCard personCard = new PersonCard(person, 1);
            return (Label) personCard.getRoot().lookup("#role");
        });
        Platform.runLater(createCard);
        return createCard.get();
    }
}
