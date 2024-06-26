/*
 Copyright © 2021-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.model.Icon;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.panteleyev.money.backend.repository.RepositoryUtil.getUuid;

@Repository
public class IconRepository implements MoneyRepository<Icon> {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<Icon> rowMapper = (rs, _) -> new Icon(
            getUuid(rs, "uuid"),
            rs.getString("name"),
            rs.getBytes("bytes"),
            rs.getLong("created"),
            rs.getLong("modified")
    );

    public IconRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Icon> getAll() {
        return jdbcTemplate.query("SELECT * FROM icon", rowMapper);
    }

    @Override
    public Stream<Icon> getStream() {
        return jdbcTemplate.queryForStream("SELECT * FROM icon", Map.of(), rowMapper);
    }

    @Override
    public int insertOrUpdate(Icon icon) {
        return jdbcTemplate.update("""
                        INSERT INTO icon (
                            uuid,
                            name,
                            bytes,
                            created,
                            modified
                        )
                        VALUES (
                            :uuid,
                            :name,
                            :bytes,
                            :created,
                            :modified
                        )
                        ON CONFLICT (uuid) DO UPDATE SET
                            name = :name,
                            bytes = :bytes,
                            modified = :modified
                        """,
                Map.of(
                        "uuid", icon.uuid(),
                        "name", icon.name(),
                        "bytes", icon.bytes(),
                        "created", icon.created(),
                        "modified", icon.modified()
                ));
    }

    @Override
    public Optional<Icon> get(UUID uuid) {
        var result = jdbcTemplate.query("""
                SELECT * FROM icon WHERE uuid = :uuid
                """, Map.of("uuid", uuid), rowMapper);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }
}
