/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.persistence;

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.CardType;
import org.panteleyev.money.model.CategoryType;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class AccountRepository extends Repository<Account> {

    AccountRepository(DataSource dataSource) {
        super("account", dataSource);
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
        st.setString(index++, account.categoryUuid().toString());
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
