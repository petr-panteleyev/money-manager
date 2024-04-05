/*
 Copyright © 2021-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.model.Account;
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
import static org.panteleyev.money.backend.repository.RepositoryUtil.convert;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getEnum;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getLocalDate;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getUuid;

@Repository
public class AccountRepository implements MoneyRepository<Account> {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<Account> rowMapper = (rs, _) -> new Account(
            getUuid(rs, "uuid"),
            rs.getString("name"),
            rs.getString("comment"),
            rs.getString("number"),
            rs.getBigDecimal("opening"),
            rs.getBigDecimal("account_limit"),
            rs.getBigDecimal("rate"),
            getEnum(rs, "type", CategoryType.class),
            getUuid(rs, "category_uuid"),
            getUuid(rs, "currency_uuid"),
            getUuid(rs, "security_uuid"),
            rs.getBoolean("enabled"),
            rs.getBigDecimal("interest"),
            getLocalDate(rs, "closing_date"),
            getUuid(rs, "icon_uuid"),
            rs.getBigDecimal("total"),
            rs.getBigDecimal("total_waiting"),
            rs.getLong("created"),
            rs.getLong("modified")
    );

    private static Map<String, Object> toMap(Account account) {
        var map = new HashMap<String, Object>(Map.ofEntries(
                entry("uuid", account.uuid()),
                entry("name", account.name()),
                entry("comment", account.comment()),
                entry("number", account.accountNumber()),
                entry("opening", account.openingBalance()),
                entry("accountLimit", account.accountLimit()),
                entry("rate", account.currencyRate()),
                entry("type", account.type().name()),
                entry("categoryUuid", account.categoryUuid()),
                entry("enabled", account.enabled()),
                entry("interest", account.interest()),
                entry("total", account.total()),
                entry("totalWaiting", account.totalWaiting()),
                entry("created", account.created()),
                entry("modified", account.modified())
        ));
        map.put("currencyUuid", account.currencyUuid());
        map.put("closingDate", convert(account.closingDate()));
        map.put("iconUuid", account.iconUuid());
        return map;
    }

    public AccountRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Account> getAll() {
        return jdbcTemplate.query("SELECT * FROM account", rowMapper);
    }

    @Override
    public Stream<Account> getStream() {
        return jdbcTemplate.queryForStream("SELECT * FROM account", Map.of(), rowMapper);
    }

    @Override
    public Optional<Account> get(UUID uuid) {
        var result = jdbcTemplate.query("""
                SELECT * FROM account WHERE uuid = :uuid
                """, Map.of("uuid", uuid), rowMapper);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    @Override
    public int insertOrUpdate(Account account) {
        return jdbcTemplate.update("""
                        INSERT INTO account (
                            uuid, name, comment, number, opening, account_limit, rate, type,
                            category_uuid, currency_uuid, enabled, interest, closing_date, icon_uuid,
                            total, total_waiting, created, modified
                        ) VALUES (
                            :uuid, :name, :comment, :number, :opening, :accountLimit, :rate, :type,
                            :categoryUuid, :currencyUuid, :enabled, :interest, :closingDate, :iconUuid,
                            :total, :totalWaiting, :created, :modified
                        )
                        ON CONFLICT (uuid) DO UPDATE SET
                            name = :name,
                            comment = :comment,
                            number = :number,
                            opening = :opening,
                            account_limit = :accountLimit,
                            rate = :rate,
                            type = :type,
                            category_uuid = :categoryUuid,
                            currency_uuid = :currencyUuid,
                            enabled = :enabled,
                            interest = :interest,
                            closing_date = :closingDate,
                            icon_uuid = :iconUuid,
                            total = :total,
                            total_waiting = :totalWaiting,
                            modified = :modified
                        """,
                toMap(account)
        );
    }

    public int getCount(boolean inactive) {
        return jdbcTemplate.query("SELECT COUNT(uuid) FROM account" + (inactive ? "" : " WHERE enabled=true"),
                Map.of(),
                (rs, _) -> rs.getInt(1)
        ).getFirst();
    }
}
