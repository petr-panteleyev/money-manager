/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.persistence;

import org.panteleyev.money.model.Icon;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class IconRepository extends Repository<Icon> {
    public IconRepository(DataSource dataSource) {
        super("icon", dataSource);
    }

    @Override
    protected String getInsertSql() {
        return """
            INSERT INTO icon (
                uuid, name, bytes, created, modified
            ) VALUES (
                ?, ?, ?, ?, ?
            )
            """;
    }

    @Override
    protected String getUpdateSql() {
        return """
            UPDATE icon SET
                name = ?,
                bytes = ?,
                created = ?,
                modified = ?
            WHERE uuid = ?
            """;
    }

    @Override
    protected Icon fromResultSet(ResultSet rs) throws SQLException {
        return new Icon(
            getUuid(rs, "uuid"),
            rs.getString("name"),
            rs.getBytes("bytes"),
            rs.getLong("created"),
            rs.getLong("modified")
        );
    }

    @Override
    protected void toStatement(PreparedStatement st, Icon icon, boolean update) throws SQLException {
        var index = 1;
        if (!update) {
            setUuid(st, index++, icon.uuid());
        }
        st.setString(index++, icon.getName());
        st.setBytes(index++, icon.getBytes());
        st.setLong(index++, icon.created());
        st.setLong(index++, icon.modified());
        if (update) {
            setUuid(st, index, icon.uuid());
        }
    }
}
