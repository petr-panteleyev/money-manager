/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.persistence;

import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class ContactRepository extends Repository<Contact> {
    ContactRepository(DataSource dataSource) {
        super("contact", dataSource);
    }

    @Override
    protected String getInsertSql() {
        return """
            INSERT INTO contact (
                uuid, name, type, phone, mobile,
                email, web, comment, street, city,
                country, zip, icon_uuid, created, modified
            ) VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
            )
            """;
    }

    @Override
    protected String getUpdateSql() {
        return """
            UPDATE contact SET
                name = ?,
                type = ?,
                phone = ?,
                mobile = ?,
                email = ?,
                web = ?,
                comment = ?,
                street = ?,
                city = ?,
                country = ?,
                zip = ?,
                icon_uuid = ?,
                created = ?,
                modified = ?
            WHERE uuid = ?
            """;
    }

    protected Contact fromResultSet(ResultSet rs) throws SQLException {
        return new Contact(
            getUuid(rs, "uuid"),
            rs.getString("name"),
            getEnum(rs, "type", ContactType.class),
            rs.getString("phone"),
            rs.getString("mobile"),
            rs.getString("email"),
            rs.getString("web"),
            rs.getString("comment"),
            rs.getString("street"),
            rs.getString("city"),
            rs.getString("country"),
            rs.getString("zip"),
            getUuid(rs, "icon_uuid"),
            rs.getLong("created"),
            rs.getLong("modified")
        );
    }

    @Override
    protected void toStatement(PreparedStatement st, Contact contact, boolean update) throws SQLException {
        var index = 1;
        if (!update) {
            st.setString(index++, contact.uuid().toString());
        }
        st.setString(index++, contact.name());
        st.setString(index++, contact.type().name());
        st.setString(index++, contact.phone());
        st.setString(index++, contact.mobile());
        st.setString(index++, contact.email());
        st.setString(index++, contact.web());
        st.setString(index++, contact.comment());
        st.setString(index++, contact.street());
        st.setString(index++, contact.city());
        st.setString(index++, contact.country());
        st.setString(index++, contact.zip());
        setUuid(st, index++, contact.iconUuid());
        st.setLong(index++, contact.created());
        st.setLong(index++, contact.modified());
        if (update) {
            st.setString(index, contact.uuid().toString());
        }
    }
}
