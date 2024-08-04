/*
 Copyright © 2021-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.persistence;

import org.panteleyev.money.model.Account;
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
                    account_limit, rate, type, category_uuid, currency_uuid, security_uuid,
                    enabled, interest, closing_date, icon_uuid,
                    total, total_waiting, created, modified, uuid
                ) VALUES (
                    ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?,
                    ?, ?, ?, ?
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
                    security_uuid = ?,
                    enabled = ?,
                    interest = ?,
                    closing_date = ?,
                    icon_uuid = ?,
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
        setUuid(st, index++, account.securityUuid());
        st.setBoolean(index++, account.enabled());
        st.setBigDecimal(index++, account.interest());
        setLocalDate(st, index++, account.closingDate());
        setUuid(st, index++, account.iconUuid());
        st.setBigDecimal(index++, account.total());
        st.setBigDecimal(index++, account.totalWaiting());
        st.setLong(index++, account.created());
        st.setLong(index++, account.modified());
        setUuid(st, index, account.uuid());
    }
}
