/*
 Copyright © 2024-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.persistence;

import org.panteleyev.money.model.exchange.ExchangeSecuritySplit;
import org.panteleyev.money.model.exchange.ExchangeSecuritySplitType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class ExchangeSecuritySplitRepository extends Repository<ExchangeSecuritySplit> {
    ExchangeSecuritySplitRepository() {
        super("exchange_security_split");
    }

    @Override
    protected String getInsertSql() {
        return """
                INSERT INTO exchange_security_split (
                    security_uuid, split_type, split_date, rate, comment,
                    created, modified, uuid
                ) VALUES (
                    ?, ?, ?, ?, ?,
                    ?, ?, ?
                )
                """;
    }

    @Override
    protected String getUpdateSql() {
        return """
                UPDATE exchange_security_split SET
                    security_uuid = ?,
                    split_type = ?,
                    split_date = ?,
                    rate = ?,
                    comment = ?,
                    created = ?,
                    modified = ?
                WHERE UUID = ?
                """;
    }

    @Override
    protected ExchangeSecuritySplit fromResultSet(ResultSet rs) throws SQLException {
        return new ExchangeSecuritySplit(
                getUuid(rs, "uuid"),
                getUuid(rs, "security_uuid"),
                getEnum(rs, "split_type", ExchangeSecuritySplitType.class),
                getLocalDate(rs, "split_date"),
                rs.getBigDecimal("rate"),
                rs.getString("comment"),
                rs.getLong("created"),
                rs.getLong("modified")
        );
    }

    @Override
    protected void toStatement(PreparedStatement st, ExchangeSecuritySplit securitySplit) throws SQLException {
        var index = 1;
        setUuid(st, index++, securitySplit.securityUuid());
        st.setString(index++, securitySplit.type().name());
        setLocalDate(st, index++, securitySplit.date());
        st.setBigDecimal(index++, securitySplit.rate());
        st.setString(index++, securitySplit.comment());
        st.setLong(index++, securitySplit.created());
        st.setLong(index++, securitySplit.modified());
        setUuid(st, index, securitySplit.uuid());
    }
}
