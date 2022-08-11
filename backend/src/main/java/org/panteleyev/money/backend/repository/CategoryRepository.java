/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getEnum;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getUuid;

@Repository
public class CategoryRepository implements MoneyRepository<Category> {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<Category> rowMapper = (rs, i) -> new Category(
            getUuid(rs, "uuid"),
            rs.getString("name"),
            rs.getString("comment"),
            getEnum(rs, "type", CategoryType.class),
            getUuid(rs, "icon_uuid"),
            rs.getLong("created"),
            rs.getLong("modified")
    );

    private static Map<String, Object> toMap(Category category) {
        var map = new HashMap<String, Object>(Map.ofEntries(
                entry("uuid", category.uuid()),
                entry("name", category.name()),
                entry("comment", category.comment()),
                entry("type", category.type().name()),
                entry("created", category.created()),
                entry("modified", category.modified())
        ));
        map.put("iconUuid", category.iconUuid());
        return map;
    }

    public CategoryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Category> getAll() {
        return jdbcTemplate.query("SELECT * FROM category", rowMapper);
    }

    @Override
    public Stream<Category> getStream() {
        return jdbcTemplate.queryForStream("SELECT * FROM category", Map.of(), rowMapper);
    }

    @Override
    public int insert(Category category) {
        return jdbcTemplate.update("""
                        INSERT INTO category (
                            uuid, name, comment, type, icon_uuid, created, modified
                        ) VALUES (
                            :uuid, :name, :comment, :type, :iconUuid, :created, :modified
                        )
                        """,
                toMap(category)
        );
    }

    public int update(Category category) {
        return jdbcTemplate.update("""
                        UPDATE category SET
                            name = :name,
                            comment = :comment,
                            type = :type,
                            icon_uuid = :iconUuid,
                            modified = :modified
                        WHERE uuid = :uuid
                        """,
                toMap(category)
        );
    }

    @Override
    public Optional<Category> get(UUID uuid) {
        var result = jdbcTemplate.query("""
                SELECT * FROM category WHERE uuid = :uuid
                """, Map.of("uuid", uuid), rowMapper);
        return result.size() == 0 ? Optional.empty() : Optional.of(result.get(0));
    }
}
