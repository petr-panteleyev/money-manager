/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.options;

import javafx.scene.paint.Color;
import org.w3c.dom.Element;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.Objects.requireNonNull;
import static org.panteleyev.money.xml.XMLUtils.appendElement;
import static org.panteleyev.money.xml.XMLUtils.createDocument;
import static org.panteleyev.money.xml.XMLUtils.readDocument;
import static org.panteleyev.money.xml.XMLUtils.writeDocument;

final class ColorSettings {
    private static final String ROOT_ELEMENT = "colors";
    private static final String COLOR_ELEMENT = "color";
    private static final String COLOR_ATTR_NAME = "name";
    private static final String COLOR_ATTR_VALUE = "value";

    private final Map<ColorOption, Color> colorMap = new ConcurrentHashMap<>();

    Color getColor(ColorOption option) {
        return colorMap.computeIfAbsent(option, key -> option.getDefaultColor());
    }

    String getWebString(ColorOption option) {
        var color = getColor(option);
        return "#"
            + colorToHex(color.getRed())
            + colorToHex(color.getGreen())
            + colorToHex(color.getBlue());
    }

    void setColor(ColorOption option, Color color) {
        colorMap.put(
            requireNonNull(option),
            requireNonNull(color)
        );
    }

    void save(File file) {
        try (var out = new FileOutputStream(file)) {
            var root = createDocument(ROOT_ELEMENT);

            for (var opt : ColorOption.values()) {
                var e = appendElement(root, COLOR_ELEMENT);
                e.setAttribute(COLOR_ATTR_NAME, opt.name());
                e.setAttribute(COLOR_ATTR_VALUE, getWebString(opt));
            }

            writeDocument(root.getOwnerDocument(), out);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    void load(File file) {
        if (!file.exists()) {
            return;
        }

        colorMap.clear();

        try (var in = new FileInputStream(file)) {
            var root = readDocument(in);

            var colorNodes = root.getElementsByTagName(COLOR_ELEMENT);
            for (int i = 0; i < colorNodes.getLength(); i++) {
                var colorElement = (Element)colorNodes.item(i);
                ColorOption.of(colorElement.getAttribute(COLOR_ATTR_NAME).toUpperCase())
                    .ifPresent(option -> colorMap.put(option, Color.valueOf(colorElement.getAttribute(COLOR_ATTR_VALUE))));
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static String colorToHex(double c) {
        var intValue = (int) (c * 255);
        var s = Integer.toString(intValue, 16);
        if (intValue < 16) {
            return "0" + s;
        } else {
            return s;
        }
    }
}
