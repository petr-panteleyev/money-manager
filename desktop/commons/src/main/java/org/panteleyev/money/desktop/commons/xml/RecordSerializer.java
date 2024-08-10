/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.commons.xml;

import org.panteleyev.commons.xml.StartElementWrapper;
import org.panteleyev.commons.xml.XMLStreamWriterWrapper;

import javax.xml.namespace.QName;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RecordSerializer {
    private static final Map<Class<? extends Record>, Map<String, Method>> ACCESSOR_MAP = new ConcurrentHashMap<>();
    private static final Map<Class<? extends Record>, Constructor<?>> CONSTRUCTOR_MAP = new ConcurrentHashMap<>();

    private static final String TYPE_BOOL = "boolean";
    private static final String TYPE_INT = "int";
    private static final String TYPE_LONG = "long";
    private static final String TYPE_DOUBLE = "double";

    public static void serialize(XMLStreamWriterWrapper wrapper, Record rec) {
        try {
            var type = rec.getClass();
            var methodMap = ACCESSOR_MAP.computeIfAbsent(type, _ -> new HashMap<>());

            wrapper.element(new QName(type.getSimpleName()), () -> {
                for (var component : type.getRecordComponents()) {
                    var name = component.getName();
                    var method = methodMap.computeIfAbsent(name, _ -> component.getAccessor());
                    try {
                        var value = method.invoke(rec);
                        if (value != null) {
                            addAttribute(wrapper, new QName(name), value);
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void addAttribute(XMLStreamWriterWrapper wrapper, QName name, Object value) {
        if (value.getClass().isArray()) {
            wrapper.attribute(name, Base64.getEncoder().encodeToString((byte[])value));
        } else {
            wrapper.attribute(name, value);
        }
    }

    public static <T extends Record> T deserializeRecord(StartElementWrapper element, Class<T> recordClass) {
        var components = recordClass.getRecordComponents();
        var arguments = new Object[components.length];

        var constructor = CONSTRUCTOR_MAP.computeIfAbsent(recordClass, RecordSerializer::getCanonicalConstructor);

        for (var i = 0; i < components.length; i++) {
            arguments[i] = getComponentValue(element, new QName(components[i].getName()), components[i].getType());
        }

        try {
            //noinspection unchecked
            return (T) constructor.newInstance(arguments);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Object getComponentValue(StartElementWrapper element, QName qName, Class<?> type) {
        if (type.isPrimitive()) {
            var typeName = type.getTypeName();

            return switch (typeName) {
                case TYPE_INT -> element.getAttributeValue(qName, 0);
                case TYPE_LONG -> element.getAttributeValue(qName, 0L);
                case TYPE_BOOL -> element.getAttributeValue(qName, false);
                case TYPE_DOUBLE -> element.getAttributeValue(qName, 0.0);
                default -> throw new IllegalArgumentException("Unsupported type: " + typeName);
            };
        } else {
            return element.getAttributeValue(qName, type).orElse(null);
        }
    }

    private static Constructor<?> getCanonicalConstructor(Class<? extends Record> recordClass) {
        try {
            var argTypes = Arrays.stream(recordClass.getRecordComponents())
                    .map(RecordComponent::getType)
                    .toArray(Class<?>[]::new);
            return recordClass.getDeclaredConstructor(argTypes);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private RecordSerializer() {
    }
}
