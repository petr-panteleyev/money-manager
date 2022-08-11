/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.persistence;

import org.panteleyev.money.model.MoneyRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

abstract class Repository<T extends MoneyRecord> {
    private final String tableName;

    public Repository(String tableName) {
        this.tableName = tableName;
    }

    abstract protected T fromResultSet(ResultSet rs) throws SQLException;

    abstract protected void toStatement(PreparedStatement st, T object) throws SQLException;

    abstract protected String getInsertSql();

    abstract protected String getUpdateSql();

    public List<T> getAll(Connection conn) {
        try (var st = conn.prepareStatement("SELECT * FROM " + tableName)) {
            var result = new ArrayList<T>();
            try (var rs = st.executeQuery()) {
                while (rs.next()) {
                    result.add(fromResultSet(rs));
                }
            }
            return result;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Optional<T> get(Connection conn, UUID uuid) {
        try (var st = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE uuid = ?")) {
            st.setObject(1, uuid);
            try (var rs = st.executeQuery()) {
                return rs.next() ? Optional.of(fromResultSet(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean insert(Connection conn, T object) {
        try (var st = conn.prepareStatement(getInsertSql())) {
            toStatement(st, object);
            return st.execute();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void insert(Connection conn, int batchSize, List<T> records) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("Batch size must be >= 1");
        }

        if (!records.isEmpty()) {
            var sql = getInsertSql();

            try (var st = conn.prepareStatement(sql)) {
                int count = 0;

                for (T r : records) {
                    toStatement(st, r);
                    st.addBatch();

                    if (++count % batchSize == 0) {
                        st.executeBatch();
                    }
                }

                st.executeBatch();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public boolean update(Connection conn, T object) {
        try (var st = conn.prepareStatement(getUpdateSql())) {
            toStatement(st, object);
            return st.execute();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int delete(Connection conn, T object) {
        try (var st = conn.prepareStatement("DELETE FROM " + tableName + " WHERE uuid = ?")) {
            st.setObject(1, object.uuid());
            return st.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    static UUID getUuid(ResultSet set, String columnLabel) throws SQLException {
        var obj = set.getObject(columnLabel);
        return (obj instanceof UUID uuid) ? uuid : null;
    }

    static void setUuid(PreparedStatement st, int index, UUID uuid) throws SQLException {
        st.setObject(index, uuid);
    }

    static <E extends Enum<E>> E getEnum(ResultSet set, String columnLabel, Class<E> eClass) throws SQLException {
        var obj = set.getObject(columnLabel);
        return obj instanceof String str ? E.valueOf(eClass, str) : null;
    }

    static void setEnum(PreparedStatement st, int index, Enum<?> value) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            st.setString(index, value.name());
        }
    }

    static LocalDate getLocalDate(ResultSet set, String columnLabel) throws SQLException {
        return set.getObject(columnLabel) == null ? null : LocalDate.ofEpochDay(set.getLong(columnLabel));
    }

    static void setLocalDate(PreparedStatement st, int index, LocalDate localDate) throws SQLException {
        if (localDate == null) {
            st.setNull(index, Types.INTEGER);
        } else {
            st.setLong(index, localDate.toEpochDay());
        }
    }
}
