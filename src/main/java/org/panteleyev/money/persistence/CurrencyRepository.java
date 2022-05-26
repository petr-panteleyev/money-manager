/*
 Copyright (C) 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.persistence;

import org.panteleyev.money.model.Currency;

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
                    symbol, description, format_symbol, format_symbol_pos,
                    show_format_symbol, def, rate, rate_direction, use_th_separator,
                    created, modified, uuid
                ) VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                )
                """;
    }

    @Override
    protected String getUpdateSql() {
        return """
                UPDATE currency SET
                    symbol = ?,
                    description = ?,
                    format_symbol = ?,
                    format_symbol_pos = ?,
                    show_format_symbol = ?,
                    def = ?,
                    rate = ?,
                    rate_direction = ?,
                    use_th_separator = ?,
                    created = ?,
                    modified = ?
                WHERE uuid = ?
                """;
    }

    @Override
    protected Currency fromResultSet(ResultSet rs) throws SQLException {
        return new Currency(
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
    }

    @Override
    protected void toStatement(PreparedStatement st, Currency currency) throws SQLException {
        var index = 1;
        st.setString(index++, currency.symbol());
        st.setString(index++, currency.description());
        st.setString(index++, currency.formatSymbol());
        st.setInt(index++, currency.formatSymbolPosition());
        setBoolean(st, index++, currency.showFormatSymbol());
        setBoolean(st, index++, currency.def());
        st.setBigDecimal(index++, currency.rate());
        st.setInt(index++, currency.direction());
        setBoolean(st, index++, currency.useThousandSeparator());
        st.setLong(index++, currency.created());
        st.setLong(index++, currency.modified());
        st.setString(index, currency.uuid().toString());
    }
}
