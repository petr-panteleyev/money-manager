/*
 Copyright © 2021-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.persistence;

import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.CurrencyType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class CurrencyRepository extends Repository<Currency> {
    CurrencyRepository() {
        super("currency");
    }

    @Override
    protected String getInsertSql() {
        return """
                INSERT INTO currency (
                    type, symbol, description, format_symbol, format_symbol_pos,
                    show_format_symbol, def, rate, rate_direction, use_th_separator,
                    isin, registry, created, modified, uuid
                ) VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                )
                """;
    }

    @Override
    protected String getUpdateSql() {
        return """
                UPDATE currency SET
                    type = ?,
                    symbol = ?,
                    description = ?,
                    format_symbol = ?,
                    format_symbol_pos = ?,
                    show_format_symbol = ?,
                    def = ?,
                    rate = ?,
                    rate_direction = ?,
                    use_th_separator = ?,
                    isin = ?,
                    registry = ?,
                    created = ?,
                    modified = ?
                WHERE uuid = ?
                """;
    }

    @Override
    protected Currency fromResultSet(ResultSet rs) throws SQLException {
        return new Currency(
                getUuid(rs, "uuid"),
                getEnum(rs, "type", CurrencyType.class),
                rs.getString("symbol"),
                rs.getString("description"),
                rs.getString("format_symbol"),
                rs.getInt("format_symbol_pos"),
                rs.getBoolean("show_format_symbol"),
                rs.getBoolean("def"),
                rs.getBigDecimal("rate"),
                rs.getInt("rate_direction"),
                rs.getBoolean("use_th_separator"),
                rs.getString("isin"),
                rs.getString("registry"),
                rs.getLong("created"),
                rs.getLong("modified")
        );
    }

    @Override
    protected void toStatement(PreparedStatement st, Currency currency) throws SQLException {
        var index = 1;
        setEnum(st, index++, currency.type());
        st.setString(index++, currency.symbol());
        st.setString(index++, currency.description());
        st.setString(index++, currency.formatSymbol());
        st.setInt(index++, currency.formatSymbolPosition());
        st.setBoolean(index++, currency.showFormatSymbol());
        st.setBoolean(index++, currency.def());
        st.setBigDecimal(index++, currency.rate());
        st.setInt(index++, currency.direction());
        st.setBoolean(index++, currency.useThousandSeparator());
        st.setString(index++, currency.isin());
        st.setString(index++, currency.registry());
        st.setLong(index++, currency.created());
        st.setLong(index++, currency.modified());
        setUuid(st, index, currency.uuid());
    }
}
