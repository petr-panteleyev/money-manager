/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.options;

import org.panteleyev.fx.Controller;
import org.panteleyev.fx.WindowManager;
import org.w3c.dom.Element;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static org.panteleyev.money.xml.XMLUtils.appendElement;
import static org.panteleyev.money.xml.XMLUtils.createDocument;
import static org.panteleyev.money.xml.XMLUtils.getAttribute;
import static org.panteleyev.money.xml.XMLUtils.readDocument;
import static org.panteleyev.money.xml.XMLUtils.writeDocument;

final class WindowsSettings {
    private static final double DEFAULT_WIDTH = 1024.0;
    private static final double DEFAULT_HEIGHT = 768.0;

    private static final String ROOT_ELEMENT = "windows";
    private static final String WINDOW_ELEMENT = "window";
    private static final String CLASS_ATTR = "class";
    private static final String X_ATTR = "x";
    private static final String Y_ATTR = "y";
    private static final String WIDTH_ATTR = "width";
    private static final String HEIGHT_ATTR = "height";
    private static final String MAXIMIZED_ATTR = "maximized";

    private static record WindowPositionAndSize(
        double x, double y,
        double width, double height, boolean maximized
    ) {
    }

    private final Map<String, WindowPositionAndSize> windowMap = new ConcurrentHashMap<>();

    void storeWindowDimensions(Controller controller) {
        var stage = controller.getStage();
        windowMap.put(controller.getClass().getSimpleName(), new WindowPositionAndSize(
            stage.getX(), stage.getY(),
            stage.getWidth(), stage.getHeight(), stage.isMaximized()
        ));
    }

    void restoreWindowDimensions(Controller controller) {
        var dimensions = windowMap.get(controller.getClass().getSimpleName());
        if (dimensions == null) {
            return;
        }

        var stage = controller.getStage();
        if (dimensions.maximized()) {
            stage.setMaximized(true);
        } else {
            stage.setX(dimensions.x());
            stage.setY(dimensions.y());
            stage.setWidth(dimensions.width());
            stage.setHeight(dimensions.height());
        }
    }

    void save(File file) {
        WindowManager.newInstance().getControllerStream().forEach(this::storeWindowDimensions);

        try (var out = new FileOutputStream(file)) {
            var root = createDocument(ROOT_ELEMENT);

            for (var entry : windowMap.entrySet()) {
                var w = appendElement(root, WINDOW_ELEMENT);
                w.setAttribute(CLASS_ATTR, entry.getKey());
                w.setAttribute(X_ATTR, Double.toString(entry.getValue().x()));
                w.setAttribute(Y_ATTR, Double.toString(entry.getValue().y()));
                w.setAttribute(WIDTH_ATTR, Double.toString(entry.getValue().width()));
                w.setAttribute(HEIGHT_ATTR, Double.toString(entry.getValue().height()));
                w.setAttribute(MAXIMIZED_ATTR, Boolean.toString(entry.getValue().maximized()));
            }

            writeDocument(root.getOwnerDocument(), out);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    void load(File file) {
        windowMap.clear();

        if (!file.exists()) {
            return;
        }

        try (var in = new FileInputStream(file)) {
            var root = readDocument(in);
            var windowNodes = root.getElementsByTagName(WINDOW_ELEMENT);
            for (int i = 0; i < windowNodes.getLength(); i++) {
                if (windowNodes.item(i) instanceof Element windowElement) {
                    var className = windowElement.getAttribute(CLASS_ATTR);
                    var x = getAttribute(windowElement, X_ATTR, 0.0);
                    var y = getAttribute(windowElement, Y_ATTR, 0.0);
                    var width = getAttribute(windowElement, WIDTH_ATTR, DEFAULT_WIDTH);
                    var height = getAttribute(windowElement, HEIGHT_ATTR, DEFAULT_HEIGHT);
                    var maximized = getAttribute(windowElement, MAXIMIZED_ATTR, false);
                    windowMap.put(className, new WindowPositionAndSize(
                        x, y, width, height, maximized
                    ));
                }
            }

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
