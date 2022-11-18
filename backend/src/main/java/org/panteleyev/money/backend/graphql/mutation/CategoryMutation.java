/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import org.panteleyev.money.backend.graphql.exception.GraphQLCreateException;
import org.panteleyev.money.backend.graphql.exception.GraphQLUpdateException;
import org.panteleyev.money.backend.graphql.input.CategoryInput;
import org.panteleyev.money.backend.service.CategoryService;
import org.panteleyev.money.model.Category;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CategoryMutation implements GraphQLMutationResolver {
    private final CategoryService service;

    public CategoryMutation(CategoryService service) {
        this.service = service;
    }

    public Category createCategory(CategoryInput input) {
        var category = new Category.Builder()
                .name(input.name())
                .type(input.type())
                .comment(input.comment())
                .iconUuid(input.iconUuid())
                .build();
        return service.put(category)
                .orElseThrow(() -> new GraphQLCreateException("Category"));
    }

    public Category updateCategory(UUID uuid, CategoryInput input) {
        var builder = service.get(uuid)
                .map(Category.Builder::new)
                .orElseThrow();

        builder.name(input.name())
                .type(input.type())
                .comment(input.comment())
                .iconUuid(input.iconUuid())
                .modified(System.currentTimeMillis());
        return service.put(builder.build())
                .orElseThrow(() -> new GraphQLUpdateException("Category", uuid));
    }
}
