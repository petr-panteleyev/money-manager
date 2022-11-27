/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.controller;

import org.panteleyev.money.backend.graphql.exception.GraphQLCreateException;
import org.panteleyev.money.backend.graphql.exception.GraphQLNotFoundException;
import org.panteleyev.money.backend.graphql.exception.GraphQLUpdateException;
import org.panteleyev.money.backend.graphql.input.CategoryInput;
import org.panteleyev.money.backend.service.CategoryService;
import org.panteleyev.money.backend.service.IconService;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Icon;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class CategoryGraphQLController {
    private final IconService iconService;
    private final CategoryService categoryService;

    public CategoryGraphQLController(
            IconService iconService,
            CategoryService categoryService
    ) {
        this.iconService = iconService;
        this.categoryService = categoryService;
    }

    @QueryMapping
    public List<Category> categories() {
        return categoryService.getAll();
    }

    @QueryMapping
    public Category category(@Argument UUID uuid) {
        return categoryService.get(uuid)
                .orElseThrow(() -> new GraphQLNotFoundException("Category", uuid));
    }

    @MutationMapping
    public Category createCategory(@Argument CategoryInput input) {
        var category = new Category.Builder()
                .name(input.name())
                .type(input.type())
                .comment(input.comment())
                .iconUuid(input.iconUuid())
                .build();
        return categoryService.put(category)
                .orElseThrow(() -> new GraphQLCreateException("Category"));
    }

    @MutationMapping
    public Category updateCategory(
            @Argument UUID uuid,
            @Argument CategoryInput input
    ) {
        var builder = categoryService.get(uuid)
                .map(Category.Builder::new)
                .orElseThrow();

        builder.name(input.name())
                .type(input.type())
                .comment(input.comment())
                .iconUuid(input.iconUuid())
                .modified(System.currentTimeMillis());
        return categoryService.put(builder.build())
                .orElseThrow(() -> new GraphQLUpdateException("Category", uuid));
    }

    @SchemaMapping
    public Icon icon(Category category) {
        return category.iconUuid() == null ?
                null : iconService.get(category.iconUuid()).orElse(null);
    }
}
