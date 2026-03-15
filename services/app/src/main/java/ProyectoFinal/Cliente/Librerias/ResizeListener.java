package ProyectoFinal.Cliente.Librerias;

import ProyectoFinal.Cliente.GestorPantallas;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ResizeListener implements EventHandler<MouseEvent> {
    private final Stage stage;
    private final HBox top;
    private Cursor cursorEvent = Cursor.DEFAULT;
    private boolean resizing = true;
    private double startX = 0;
    private double startY = 0;
    private double screenOffsetX = 0;
    private double screenOffsetY = 0;

    // Max and min sizes for controlled stage
    private double minWidth;
    private double maxWidth;
    private double minHeight;
    private double maxHeight;
    private static boolean maximized;
    private static boolean inicioSesion;

    public ResizeListener(Stage stage) {
        this.stage = stage;
        this.top = (HBox) stage.getScene().lookup("#barraTopMove");
    }

    public void setMinWidth(double minWidth) {
        this.minWidth = minWidth;
    }

    public void setMaxWidth(double maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void setMinHeight(double minHeight) {
        this.minHeight = minHeight;
    }

    public void setMaxHeight(double maxHeight) {
        this.maxHeight = maxHeight;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        EventType<? extends MouseEvent> mouseEventType = mouseEvent.getEventType();
        Scene scene = stage.getScene();

        double mouseEventX = mouseEvent.getSceneX(),
                mouseEventY = mouseEvent.getSceneY(),
                sceneWidth = scene.getWidth(),
                sceneHeight = scene.getHeight();

        int border = 4;
        if (MouseEvent.MOUSE_MOVED.equals(mouseEventType)) {
            if (mouseEventX < border && mouseEventY < border) {
                cursorEvent = Cursor.NW_RESIZE;
            } else if (mouseEventX < border && mouseEventY > sceneHeight - border) {
                cursorEvent = Cursor.SW_RESIZE;
            } else if (mouseEventX > sceneWidth - border && mouseEventY < border) {
                cursorEvent = Cursor.NE_RESIZE;
            } else if (mouseEventX > sceneWidth - border && mouseEventY > sceneHeight - border) {
                cursorEvent = Cursor.SE_RESIZE;
            } else if (mouseEventX < border) {
                cursorEvent = Cursor.W_RESIZE;
            } else if (mouseEventX > sceneWidth - border) {
                cursorEvent = Cursor.E_RESIZE;
            } else if (mouseEventY < border) {
                cursorEvent = Cursor.N_RESIZE;
            } else if (mouseEventY > sceneHeight - border) {
                cursorEvent = Cursor.S_RESIZE;
            } else {
                cursorEvent = Cursor.DEFAULT;
            }
            scene.setCursor(cursorEvent);
        } else if (MouseEvent.MOUSE_EXITED.equals(mouseEventType) || MouseEvent.MOUSE_EXITED_TARGET.equals(mouseEventType)) {
            scene.setCursor(Cursor.DEFAULT);
        } else if (MouseEvent.MOUSE_PRESSED.equals(mouseEventType)) {
            startX = stage.getWidth() - mouseEventX;
            startY = stage.getHeight() - mouseEventY;
        } else if (MouseEvent.MOUSE_DRAGGED.equals(mouseEventType)) {
            if (!Cursor.DEFAULT.equals(cursorEvent)) {
                resizing = true;
                if (!Cursor.W_RESIZE.equals(cursorEvent) && !Cursor.E_RESIZE.equals(cursorEvent)) {
                    double minHeight = stage.getMinHeight() > (border * 2) ? stage.getMinHeight() : (border * 2);
                    if (Cursor.NW_RESIZE.equals(cursorEvent) || Cursor.N_RESIZE.equals(cursorEvent)
                            || Cursor.NE_RESIZE.equals(cursorEvent)) {
                        if (stage.getHeight() > minHeight || mouseEventY < 0) {
                            maximized = false;
                            GestorPantallas.setMaximized(false);
                            setStageHeight(stage.getY() - mouseEvent.getScreenY() + stage.getHeight());
                            stage.setY(mouseEvent.getScreenY());
                        }
                    } else {
                        if (stage.getHeight() > minHeight || mouseEventY + startY - stage.getHeight() > 0) {
                            setStageHeight(mouseEventY + startY);
                            maximized = false;
                            GestorPantallas.setMaximized(false);
                        }
                    }
                }

                if (!Cursor.N_RESIZE.equals(cursorEvent) && !Cursor.S_RESIZE.equals(cursorEvent)) {
                    double minWidth = stage.getMinWidth() > (border * 2) ? stage.getMinWidth() : (border * 2);
                    if (Cursor.NW_RESIZE.equals(cursorEvent) || Cursor.W_RESIZE.equals(cursorEvent)
                            || Cursor.SW_RESIZE.equals(cursorEvent)) {
                        if (stage.getWidth() > minWidth || mouseEventX < 0) {
                            maximized = false;
                            GestorPantallas.setMaximized(false);
                            setStageWidth(stage.getX() - mouseEvent.getScreenX() + stage.getWidth());
                            stage.setX(mouseEvent.getScreenX());
                        }
                    } else {
                        if (stage.getWidth() > minWidth || mouseEventX + startX - stage.getWidth() > 0) {
                            setStageWidth(mouseEventX + startX);
                            maximized = false;
                            GestorPantallas.setMaximized(false);
                        }
                    }
                }
                resizing = false;
            }
        }

        if (MouseEvent.MOUSE_PRESSED.equals(mouseEventType) && Cursor.DEFAULT.equals(cursorEvent)) {
            try {
                if (mouseEvent.getSceneY() < top.getHeight()) {
                    resizing = false;
                    screenOffsetX = stage.getX() - mouseEvent.getScreenX();
                    screenOffsetY = stage.getY() - mouseEvent.getScreenY();
                }
            }catch (Exception e){
                System.err.println("Error al mover la ventana: " + e.getMessage());
            }
        }

        if (MouseEvent.MOUSE_DRAGGED.equals(mouseEventType) && Cursor.DEFAULT.equals(cursorEvent) && !resizing && !maximized) {
            if (mouseEvent.getSceneY() < top.getHeight()) {
                stage.setX(mouseEvent.getScreenX() + screenOffsetX);
                stage.setY(mouseEvent.getScreenY() + screenOffsetY);
            }
        }

    }

    private void setStageWidth(double width) {
        if (inicioSesion) {
            return;
        }
        width = Math.min(width, maxWidth);
        width = Math.max(width, minWidth);
        stage.setWidth(width);
    }

    private void setStageHeight(double height) {
        if (inicioSesion) {
            return;
        }
        height = Math.min(height, maxHeight);
        height = Math.max(height, minHeight);
        stage.setHeight(height);
    }

    public static void setMaximized(boolean maximized) {
        ResizeListener.maximized = maximized;
    }

    public static void setInicioSesion(boolean inicioSesion) {
        ResizeListener.inicioSesion = inicioSesion;
    }
}
