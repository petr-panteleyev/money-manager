/*
 Copyright © 2021-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.persistence;

import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.CardType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class CardRepository extends Repository<Card> {

    CardRepository() {
        super("card");
    }

    @Override
    protected String getInsertSql() {
        return """
                INSERT INTO card (
                    account_uuid, type, number, expiration, comment,
                    enabled, created, modified, uuid
                ) VALUES (
                    ?, ?, ?, ?, ?,
                    ?, ?, ?, ?
                )
                """;
    }

    @Override
    protected String getUpdateSql() {
        return """
                UPDATE card SET
                    account_uuid = ?,
                    type = ?,
                    number = ?,
                    expiration = ?,
                    comment = ?,
                    enabled = ?,
                    created = ?,
                    modified = ?
                WHERE uuid = ?
                """;
    }

    @Override
    protected Card fromResultSet(ResultSet rs) throws SQLException {
        return new Card(
                getUuid(rs, "uuid"),
                getUuid(rs, "account_uuid"),
                getEnum(rs, "type", CardType.class),
                rs.getString("number"),
                getLocalDate(rs, "expiration"),
                rs.getString("comment"),
                rs.getBoolean("enabled"),
                rs.getLong("created"),
                rs.getLong("modified")
        );
    }

    @Override
    protected void toStatement(PreparedStatement st, Card card) throws SQLException {
        var index = 1;
        setUuid(st, index++, card.accountUuid());
        setEnum(st, index++, card.type());
        st.setString(index++, card.number());
        setLocalDate(st, index++, card.expiration());
        st.setString(index++, card.comment());
        st.setBoolean(index++, card.enabled());
        st.setLong(index++, card.created());
        st.setLong(index++, card.modified());
        setUuid(st, index, card.uuid());
    }
}
