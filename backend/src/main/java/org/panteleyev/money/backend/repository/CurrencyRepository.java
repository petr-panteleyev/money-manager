/*
 Copyright © 2021 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.model.Currency;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.panteleyev.money.backend.repository.RepositoryUtil.convert;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getBoolean;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getUuid;

@Repository
public class CurrencyRepository implements MoneyRepository<Currency> {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<Currency> rowMapper = (rs, i) -> new Currency(
        getUuid(rs, "uuid"),
        rs.getString("symbol"),
        rs.getString("description"),
        rs.getString("format_symbol"),
        rs.getInt("format_symbol_pos"),
        getBoolean(rs, "show_format_symbol"),
        getBoolean(rs, "def"),
        rs.getBigDecimal("rate"),
        rs.getInt("rate_direction"),
        getBoolean(rs, "use_th_separator"),
        rs.getLong("created"),
        rs.getLong("modified")
    );

    public CurrencyRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Currency> getAll() {
        return jdbcTemplate.query("SELECT * FROM currency", rowMapper);
    }

    @Override
    public int insert(Currency currency) {
        return jdbcTemplate.update("""
                INSERT INTO currency (
                    uuid, symbol, description, format_symbol, format_symbol_pos, show_format_symbol, def,
                    rate, rate_direction, use_th_separator, created, modified
                ) VALUES (
                    :uuid, :symbol, :description, :formatSymbol, :formatSymbolPos, :showFormatSymbol, :def,
                    :rate, :rateDirection, :useThousandSeparator, :created, :modified
                )
                """,
            Map.ofEntries(
                Map.entry("uuid", currency.uuid().toString()),
                Map.entry("symbol", currency.symbol()),
                Map.entry("description", currency.description()),
                Map.entry("formatSymbol", currency.formatSymbol()),
                Map.entry("formatSymbolPos", currency.formatSymbolPosition()),
                Map.entry("showFormatSymbol", convert(currency.showFormatSymbol())),
                Map.entry("def", convert(currency.def())),
                Map.entry("rate", currency.rate()),
                Map.entry("rateDirection", currency.direction()),
                Map.entry("useThousandSeparator", convert(currency.useThousandSeparator())),
                Map.entry("created", currency.created()),
                Map.entry("modified", currency.modified())
            ));
    }

    @Override
    public int update(Currency currency) {
        return jdbcTemplate.update("""
                UPDATE currency SET
                    symbol = :symbol,
                    description = :description,
                    format_symbol = :formatSymbol,
                    format_symbol_pos = :formatSymbolPos,
                    show_format_symbol = :showFormatSymbol,
                    def = :def,
                    rate = :rate,
                    rate_direction = :rateDirection,
                    use_th_separator = :useThousandSeparator,
                    modified = :modified
                WHERE uuid = :uuid
                """,
            Map.ofEntries(
                Map.entry("uuid", currency.uuid().toString()),
                Map.entry("symbol", currency.symbol()),
                Map.entry("description", currency.description()),
                Map.entry("formatSymbol", currency.formatSymbol()),
                Map.entry("formatSymbolPos", currency.formatSymbolPosition()),
                Map.entry("showFormatSymbol", convert(currency.showFormatSymbol())),
                Map.entry("def", convert(currency.def())),
                Map.entry("rate", currency.rate()),
                Map.entry("rateDirection", currency.direction()),
                Map.entry("useThousandSeparator", convert(currency.useThousandSeparator())),
                Map.entry("modified", currency.modified())
            ));
    }

    @Override
    public Optional<Currency> get(UUID uuid) {
        var queryResult = jdbcTemplate.query(
            "SELECT * FROM currency WHERE uuid = :id",
            Map.of("id", uuid.toString()),
            rowMapper);
        return queryResult.size() == 0 ?
            Optional.empty() :
            Optional.of(queryResult.get(0));
    }
}
