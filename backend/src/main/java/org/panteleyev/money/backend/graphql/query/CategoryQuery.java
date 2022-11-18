/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import org.panteleyev.money.backend.graphql.exception.GraphQLNotFoundException;
import org.panteleyev.money.backend.service.CategoryService;
import org.panteleyev.money.model.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CategoryQuery implements GraphQLQueryResolver {
    private final CategoryService service;

    public CategoryQuery(CategoryService service) {
        this.service = service;
    }

    public List<Category> categories() {
        return service.getAll();
    }

    public Category category(UUID uuid) {
        return service.get(uuid)
                .orElseThrow(() -> new GraphQLNotFoundException("Category", uuid));
    }
}
