/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import org.panteleyev.money.backend.service.IconService;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Icon;
import org.springframework.stereotype.Component;

@Component
public class CategoryResolver implements GraphQLResolver<Category> {
    private final IconService iconService;

    public CategoryResolver(IconService iconService) {
        this.iconService = iconService;
    }

    public Icon getIcon(Category category) {
        return category.iconUuid() == null ?
                null : iconService.get(category.iconUuid()).orElse(null);
    }
}
