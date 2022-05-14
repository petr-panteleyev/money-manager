/*
 Copyright (c) 2017-2022, Petr Panteleyev

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

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.CardType;
import org.panteleyev.money.model.CategoryType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class AccountRepository extends Repository<Account> {

    AccountRepository() {
        super("account");
    }

    @Override
    protected String getInsertSql() {
        return """
            INSERT INTO account (
                name, comment, number, opening,
                account_limit, rate, type, category_uuid, currency_uuid,
                enabled, interest, closing_date, icon_uuid, card_type,
                card_number, total, total_waiting, created, modified, uuid
            ) VALUES (
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?
            )
            """;
    }

    @Override
    protected String getUpdateSql() {
        return """
            UPDATE account SET
                name = ?,
                comment = ?,
                number = ?,
                opening = ?,
                account_limit = ?,
                rate = ?,
                type = ?,
                category_uuid = ?,
                currency_uuid = ?,
                enabled = ?,
                interest = ?,
                closing_date = ?,
                icon_uuid = ?,
                card_type = ?,
                card_number = ?,
                total = ?,
                total_waiting = ?,
                created = ?,
                modified = ?
            WHERE uuid = ?
            """;
    }

    @Override
    protected Account fromResultSet(ResultSet rs) throws SQLException {
        return new Account(
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
            getBoolean(rs, "enabled"),
            rs.getBigDecimal("interest"),
            getLocalDate(rs, "closing_date"),
            getUuid(rs, "icon_uuid"),
            getEnum(rs, "card_type", CardType.class),
            rs.getString("card_number"),
            rs.getBigDecimal("total"),
            rs.getBigDecimal("total_waiting"),
            rs.getLong("created"),
            rs.getLong("modified")
        );
    }

    @Override
    protected void toStatement(PreparedStatement st, Account account) throws SQLException {
        var index = 1;
        st.setString(index++, account.name());
        st.setString(index++, account.comment());
        st.setString(index++, account.accountNumber());
        st.setBigDecimal(index++, account.openingBalance());
        st.setBigDecimal(index++, account.accountLimit());
        st.setBigDecimal(index++, account.currencyRate());
        st.setString(index++, account.type().name());
        setUuid(st, index++, account.categoryUuid());
        setUuid(st, index++, account.currencyUuid());
        setBoolean(st, index++, account.enabled());
        st.setBigDecimal(index++, account.interest());
        setLocalDate(st, index++, account.closingDate());
        setUuid(st, index++, account.iconUuid());
        st.setString(index++, account.cardType().name());
        st.setString(index++, account.cardNumber());
        st.setBigDecimal(index++, account.total());
        st.setBigDecimal(index++, account.totalWaiting());
        st.setLong(index++, account.created());
        st.setLong(index++, account.modified());
        setUuid(st, index, account.uuid());
    }
}
