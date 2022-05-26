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

import org.panteleyev.money.model.Icon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class IconRepository extends Repository<Icon> {
    public IconRepository() {
        super("icon");
    }

    @Override
    protected String getInsertSql() {
        return """
                INSERT INTO icon (
                    name, bytes, created, modified, uuid
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
    protected void toStatement(PreparedStatement st, Icon icon) throws SQLException {
        var index = 1;
        st.setString(index++, icon.name());
        st.setBytes(index++, icon.bytes());
        st.setLong(index++, icon.created());
        st.setLong(index++, icon.modified());
        setUuid(st, index, icon.uuid());
    }
}
