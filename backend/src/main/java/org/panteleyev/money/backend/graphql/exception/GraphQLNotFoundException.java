/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.exception;

import graphql.ErrorType;

import java.util.UUID;

public class GraphQLNotFoundException extends GraphQLBaseException {
    public GraphQLNotFoundException(String type, UUID uuid) {
        super(type + " " + uuid + " not found", ErrorType.DataFetchingException);
    }
}
