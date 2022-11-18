/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.type;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class LocalDateScalar extends GraphQLScalarType {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public LocalDateScalar() {
        super("LocalDate", "java.time.LocalDate", new Coercing<LocalDate, String>() {
            @Override
            public String serialize(Object o) throws CoercingSerializeException {
                return o instanceof LocalDate date ? DATE_FORMATTER.format(date) : null;
            }

            @Override
            public LocalDate parseValue(Object o) throws CoercingParseValueException {
                return o == null ? null : LocalDate.parse(o.toString(), DATE_FORMATTER);
            }

            @Override
            public LocalDate parseLiteral(Object o) throws CoercingParseLiteralException {
                return o instanceof StringValue stringValue ?
                        LocalDate.parse(stringValue.getValue(), DATE_FORMATTER) : null;
            }
        });
    }
}
