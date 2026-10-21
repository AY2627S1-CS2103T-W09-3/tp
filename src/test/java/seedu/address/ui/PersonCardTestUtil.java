package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * Checks card content against the actual font metrics instead of platform-specific pixel heights.
 */
final class PersonCardTestUtil {
    private PersonCardTestUtil() {
    }

    static void assertCardContentFits(Region card) {
        VBox content = (VBox) card.lookup("#cardContent");
        List<Node> rows = content.getChildren().stream().filter(Node::isManaged).toList();
        Bounds cardBounds = card.localToScene(card.getLayoutBounds());
        double previousBottom = cardBounds.getMinY();
        for (Node row : rows) {
            Bounds bounds = row.localToScene(row.getLayoutBounds());
            assertTrue(bounds.getMinY() >= previousBottom - 1, row.getId() + " overlaps the previous row");
            assertTrue(bounds.getMaxY() <= cardBounds.getMaxY() + 1, row.getId() + " exceeds the card height");
            assertTrue(bounds.getMinX() >= cardBounds.getMinX() - 1
                    && bounds.getMaxX() <= cardBounds.getMaxX() + 1, row.getId() + " exceeds the card width");
            previousBottom = bounds.getMaxY();
        }

        for (Node node : card.lookupAll(".label")) {
            Label label = (Label) node;
            if (!label.isVisible() || !label.isManaged() || label.getText().isEmpty()) {
                continue;
            }
            Text text = new Text(label.getText());
            text.setFont(label.getFont());
            double requiredHeight = text.getLayoutBounds().getHeight()
                    + label.getInsets().getTop() + label.getInsets().getBottom()
                    + label.getLabelPadding().getTop() + label.getLabelPadding().getBottom();
            String context = label.getText() + " using " + label.getFont();
            assertTrue(label.getHeight() >= requiredHeight - 1, context + " has clipped text");

            Node row = label;
            while (row.getParent() != content) {
                row = row.getParent();
            }
            Bounds rowBounds = row.localToScene(row.getLayoutBounds());
            Bounds labelBounds = label.localToScene(label.getLayoutBounds());
            assertTrue(labelBounds.getMinY() >= rowBounds.getMinY() - 1
                    && labelBounds.getMaxY() <= rowBounds.getMaxY() + 1, context + " exceeds its row height");
        }
        double verticalInsets = card.getInsets().getTop() + card.getInsets().getBottom();
        assertEquals(content.getHeight() + verticalInsets, card.getHeight(), 0.01,
                "The card must wrap its laid-out content and vertical insets");
    }
}
