/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.exception;

import graphql.ErrorType;

public class GraphQLCreateException extends GraphQLBaseException {
    public GraphQLCreateException(String type) {
        super("Unable to create " + type, ErrorType.DataFetchingException);
    }
}
