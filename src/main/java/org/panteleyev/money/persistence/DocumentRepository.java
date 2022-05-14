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

import org.panteleyev.money.model.DocumentType;
import org.panteleyev.money.model.MoneyDocument;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

final class DocumentRepository extends Repository<MoneyDocument> {
    private static final String SQL_INSERT_BYTES = """
            UPDATE document SET content=? WHERE uuid=?
            """;
    private static final String SQL_GET_BYTES = """
            SELECT content FROM document WHERE uuid=?
            """;

    DocumentRepository() {
        super("document");
    }

    @Override
    protected String getInsertSql() {
        return """
                INSERT INTO document (
                    owner_uuid,
                    contact_uuid,
                    document_type,
                    file_name,
                    file_date,
                    file_size,
                    mime_type,
                    description,
                    created,
                    modified,
                    uuid
                ) VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?,?,?,?
                )
                """;
    }

    @Override
    protected String getUpdateSql() {
        return """
                UPDATE document SET
                    owner_uuid = ?,
                    contact_uuid = ?,
                    document_type = ?,
                    file_name = ?,
                    file_date = ?,
                    file_size = ?,
                    mime_type = ?,
                    description = ?,
                    created = ?,
                    modified = ?
                WHERE uuid = ?
                """;
    }

    @Override
    protected MoneyDocument fromResultSet(ResultSet rs) throws SQLException {
        return new MoneyDocument(
                getUuid(rs, "uuid"),
                getUuid(rs, "owner_uuid"),
                getUuid(rs, "contact_uuid"),
                getEnum(rs, "document_type", DocumentType.class),
                rs.getString("file_name"),
                getLocalDate(rs, "file_date"),
                rs.getInt("file_size"),
                rs.getString("mime_type"),
                rs.getString("description"),
                rs.getLong("created"),
                rs.getLong("modified")
        );
    }

    @Override
    protected void toStatement(PreparedStatement st, MoneyDocument document) throws SQLException {
        var index = 1;
        st.setString(index++, document.ownerUuid().toString());
        st.setString(index++, document.contactUuid().toString());
        st.setString(index++, document.documentType().name());
        st.setString(index++, document.fileName());
        setLocalDate(st, index++, document.date());
        st.setInt(index++, document.size());
        st.setString(index++, document.mimeType());
        st.setString(index++, document.description());
        st.setLong(index++, document.created());
        st.setLong(index++, document.modified());
        st.setString(index, document.uuid().toString());
    }

    void insertBytes(Connection conn, UUID uuid, byte[] bytes) {
        try (var st = conn.prepareStatement(SQL_INSERT_BYTES)) {
            st.setBytes(1, bytes);
            st.setString(2, uuid.toString());
            st.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    Optional<byte[]> getBytes(Connection conn, UUID uuid) {
        try (var st = conn.prepareStatement(SQL_GET_BYTES)) {
            st.setString(1, uuid.toString());
            try (var rs = st.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getBytes(1));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
