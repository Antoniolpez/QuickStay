package ProyectoFinal.Cliente.Librerias;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Util class to handle window resizing when a stage style set to StageStyle.UNDECORATED.
 * Includes dragging of the stage.
 * Original on 6/13/14.
 * Updated on 8/15/17.
 * Updated on 12/19/19.
 *
 * @author Alexander.Berg
 * @author Evgenii Kanivets
 * @author Zachary Perales
 */

public class Resize {
    private static ResizeListener resizeListener;
    public static void addResizeListener(Stage stage) {
        addResizeListener(stage, 1, 1, Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public static void addResizeListener(Stage stage, double minWidth, double minHeight, double maxWidth, double maxHeight) {
        resizeListener = new ResizeListener(stage);
        stage.getScene().addEventHandler(MouseEvent.MOUSE_MOVED, resizeListener);
        stage.getScene().addEventHandler(MouseEvent.MOUSE_PRESSED, resizeListener);
        stage.getScene().addEventHandler(MouseEvent.MOUSE_DRAGGED, resizeListener);
        stage.getScene().addEventHandler(MouseEvent.MOUSE_EXITED, resizeListener);
        stage.getScene().addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, resizeListener);

        resizeListener.setMinWidth(minWidth);
        resizeListener.setMinHeight(minHeight);
        resizeListener.setMaxWidth(maxWidth);
        resizeListener.setMaxHeight(maxHeight);

        ObservableList<Node> children = stage.getScene().getRoot().getChildrenUnmodifiable();
        for (Node child : children) {
            addListenerDeeply(child, resizeListener);
        }
    }

    private static void addListenerDeeply(Node node, EventHandler<MouseEvent> listener) {
        node.addEventHandler(MouseEvent.MOUSE_MOVED, listener);
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, listener);
        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, listener);
        node.addEventHandler(MouseEvent.MOUSE_EXITED, listener);
        node.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, listener);
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            ObservableList<Node> children = parent.getChildrenUnmodifiable();
            for (Node child : children) {
                addListenerDeeply(child, listener);
            }
        }
    }
}