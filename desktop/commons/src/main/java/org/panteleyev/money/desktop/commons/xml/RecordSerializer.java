/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.commons.xml;

import org.xml.sax.Attributes;

import javax.xml.stream.XMLStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.panteleyev.money.desktop.commons.xml.XMLUtils.DATE_FORMATTER;
import static org.panteleyev.money.desktop.commons.xml.XMLUtils.DATE_TIME_FORMATTER;
import static org.panteleyev.money.desktop.commons.xml.XMLUtils.createAttribute;

@SuppressWarnings("rawtypes")
public final class RecordSerializer {
    private static final Map<Class<? extends Record>, Map<String, Method>> ACCESSOR_MAP = new ConcurrentHashMap<>();
    private static final Map<Class<? extends Record>, Constructor<?>> CONSTRUCTOR_MAP = new ConcurrentHashMap<>();

    private static final String TYPE_STRING = "java.lang.String";
    private static final String TYPE_BIG_DECIMAL = "java.math.BigDecimal";
    private static final String TYPE_BOOLEAN = "java.lang.Boolean";
    private static final String TYPE_BOOL = "boolean";
    private static final String TYPE_INTEGER = "java.lang.Integer";
    private static final String TYPE_INT = "int";
    private static final String TYPE_LONG = "java.lang.Long";
    private static final String TYPE_LONG_P = "long";
    private static final String TYPE_UUID = "java.util.UUID";
    private static final String TYPE_LOCAL_DATE = "java.time.LocalDate";
    private static final String TYPE_LOCAL_DATE_TIME = "java.time.LocalDateTime";
    private static final String TYPE_BYTE_ARRAY = "byte[]";

    public static void serialize(XMLStreamWriter writer, Record rec) {
        try {
            var clazz = rec.getClass();
            var methodMap = ACCESSOR_MAP.computeIfAbsent(clazz, _ -> new HashMap<>());

            writer.writeStartElement(clazz.getSimpleName());

            for (var component : clazz.getRecordComponents()) {
                var name = component.getName();
                var method = methodMap.computeIfAbsent(name, _ -> component.getAccessor());
                var value = method.invoke(rec);
                if (value != null) {
                    addAttribute(writer, name, value);
                }
            }

            writer.writeEndElement();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void addAttribute(XMLStreamWriter writer, String name, Object value) throws Exception {
        if (value.getClass().isArray()) {
            createAttribute(writer, name, (byte[]) value);
        } else {
            switch (value) {
                case UUID uuid -> createAttribute(writer, name, uuid);
                case String string -> createAttribute(writer, name, string);
                case Integer integerValue -> createAttribute(writer, name, integerValue);
                case Boolean booleanValue -> createAttribute(writer, name, booleanValue);
                case Long longValue -> createAttribute(writer, name, longValue);
                case LocalDate localDate -> createAttribute(writer, name, localDate);
                case LocalDateTime localDateTime -> createAttribute(writer, name, localDateTime);
                case BigDecimal bigDecimal -> createAttribute(writer, name, bigDecimal);
                case Enum<?> enumValue -> createAttribute(writer, name, enumValue);
                default ->
                        throw new IllegalArgumentException("Unsupported component type: " + value.getClass().getName());
            }
        }
    }

    public static <T extends Record> T deserializeRecord(Attributes attributes, Class<T> recordClass) {
        var components = recordClass.getRecordComponents();
        var arguments = new Object[components.length];

        var constructor = CONSTRUCTOR_MAP.computeIfAbsent(recordClass, RecordSerializer::getCanonicalConstructor);

        for (var i = 0; i < components.length; i++) {
            var stringValue = attributes.getValue(components[i].getName());
            arguments[i] = getValueFromString(components[i].getType(), stringValue);
        }

        try {
            //noinspection unchecked
            return (T) constructor.newInstance(arguments);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
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

    private static Object getValueFromString(Class type, String stringValue) {
        return stringValue == null ? fromNull(type) : fromString(type, stringValue);
    }

    static Object fromNull(Class type) {
        return type.isPrimitive() ?
                (Objects.equals(type.getTypeName(), TYPE_BOOL) ? false : 0) : null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static Object fromString(Class type, String stringValue) {
        if (type.isEnum()) {
            return Enum.valueOf(type, stringValue);
        } else {
            return switch (type.getTypeName()) {
                case TYPE_STRING -> stringValue;
                case TYPE_INT -> Integer.parseInt(stringValue);
                case TYPE_INTEGER -> Integer.valueOf(stringValue);
                case TYPE_LONG_P -> Long.parseLong(stringValue);
                case TYPE_LONG -> Long.valueOf(stringValue);
                case TYPE_BOOL -> Boolean.parseBoolean(stringValue);
                case TYPE_BOOLEAN -> Boolean.valueOf(stringValue);
                case TYPE_BIG_DECIMAL -> new BigDecimal(stringValue);
                case TYPE_UUID -> UUID.fromString(stringValue);
                case TYPE_LOCAL_DATE -> LocalDate.parse(stringValue, DATE_FORMATTER);
                case TYPE_LOCAL_DATE_TIME -> LocalDateTime.parse(stringValue, DATE_TIME_FORMATTER);
                case TYPE_BYTE_ARRAY -> Base64.getDecoder().decode(stringValue);
                default -> throw new IllegalArgumentException("Unsupported type: " + type.getTypeName());
            };
        }
    }

    private RecordSerializer() {
    }
}
