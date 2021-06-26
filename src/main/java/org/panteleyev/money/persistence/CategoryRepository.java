/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.persistence;

import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class CategoryRepository extends Repository<Category> {
    CategoryRepository(DataSource dataSource) {
        super("category", dataSource);
    }

    @Override
    protected String getInsertSql() {
        return """
            INSERT INTO category (
                uuid, name, comment, type, icon_uuid,
                created, modified
            ) VALUES (
                ?, ?, ?, ?, ?, ?, ?
            )
            """;
    }

    @Override
    protected String getUpdateSql() {
        return """
            UPDATE category SET
                name = ?,
                comment = ?,
                type = ?,
                icon_uuid = ?,
                created = ?,
                modified = ?
            WHERE uuid = ?
            """;
    }

    @Override
    protected Category fromResultSet(ResultSet rs) throws SQLException {
        return new Category(
            getUuid(rs, "uuid"),
            rs.getString("name"),
            rs.getString("comment"),
            getEnum(rs, "type", CategoryType.class),
            getUuid(rs, "icon_uuid"),
            rs.getLong("created"),
            rs.getLong("modified")
        );
    }

    @Override
    protected void toStatement(PreparedStatement st, Category category, boolean update) throws SQLException {
        var index = 1;
        if (!update) {
            setUuid(st, index++, category.uuid());
        }
        st.setString(index++, category.name());
        st.setString(index++, category.comment());
        st.setString(index++, category.type().name());
        setUuid(st, index++, category.iconUuid());
        st.setLong(index++, category.created());
        st.setLong(index++, category.modified());
        if (update) {
            setUuid(st, index, category.uuid());
        }
    }
}
