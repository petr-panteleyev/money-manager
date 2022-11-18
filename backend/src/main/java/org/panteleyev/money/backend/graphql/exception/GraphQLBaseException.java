/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.exception;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.List;

public class GraphQLBaseException extends RuntimeException implements GraphQLError {
    private final ErrorType errorType;

    public GraphQLBaseException(String message, ErrorType errorType) {
        super(message, null, false, false);
        this.errorType = errorType;
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return List.of();
    }
}
