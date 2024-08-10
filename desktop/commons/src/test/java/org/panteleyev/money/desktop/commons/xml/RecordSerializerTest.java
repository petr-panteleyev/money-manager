/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.commons.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.commons.xml.XMLEventReaderWrapper;
import org.panteleyev.commons.xml.XMLStreamWriterWrapper;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RecordSerializerTest {
    private static List<Arguments> testSerializeDeserializeArguments() {
        return List.of(
                Arguments.of(
                        new TestRecord(
                                100, 101, 100L, true, 1.1,
                                "123123123", UUID.randomUUID(), BigDecimal.TEN,
                                LocalDate.now(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                                new byte[]{1, 2, 3, 4, 5}
                        )
                ),
                Arguments.of(
                        new TestRecord(
                                100, null, 100L, true, 1.1,
                                null, null, null,
                                null, null,
                                null
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testSerializeDeserializeArguments")
    public void testSerializeDeserialize(TestRecord testRecord) throws Exception {
        try (var out = new ByteArrayOutputStream(); var writer = XMLStreamWriterWrapper.newInstance(out)) {
            writer.document(new QName("Test"), () -> {
                RecordSerializer.serialize(writer, testRecord);
            });

            try (var in = new ByteArrayInputStream(out.toByteArray());
                 var reader = XMLEventReaderWrapper.newInstance(in))
            {
                TestRecord deserializedRecord = null;

                while (reader.hasNext()) {
                    var event = reader.nextEvent();
                    var recordElement = event.asStartElement(new QName("TestRecord"));
                    if (recordElement.isPresent()) {
                        deserializedRecord = RecordSerializer.deserializeRecord(recordElement.get(), TestRecord.class);
                        assertEquals(testRecord, deserializedRecord);
                    }
                }

                assertNotNull(deserializedRecord);
            }
        }
    }

    @Test
    public void testDefaults() throws Exception {
        var xml = """
                <?xml version="1.0"?>
                <TestRecord/>
                """;

        try (var in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
             var reader = XMLEventReaderWrapper.newInstance(in))
        {
            TestRecord deserializedRecord = null;

            while (reader.hasNext()) {
                var event = reader.nextEvent();
                var recordElement = event.asStartElement(new QName("TestRecord"));
                if (recordElement.isPresent()) {
                    deserializedRecord = RecordSerializer.deserializeRecord(recordElement.get(), TestRecord.class);
                    assertEquals(new TestRecord(
                            0, null, 0, false, 0,
                            null, null, null,
                            null, null,
                            null
                    ), deserializedRecord);
                }
            }

            assertNotNull(deserializedRecord);
        }
    }
}
