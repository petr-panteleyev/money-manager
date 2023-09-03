/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.persistence;

import org.panteleyev.money.model.exchange.ExchangeSecurity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class ExchangeSecurityRepository extends Repository<ExchangeSecurity> {
    ExchangeSecurityRepository() {
        super("exchange_security");
    }

    @Override
    protected String getInsertSql() {
        return """
                INSERT INTO exchange_security (
                    sec_id, name, short_name, isin, reg_number,
                    face_value, issue_date, mat_date, days_to_redemption, group_type,
                    group_name, type, type_name, market_value, coupon_value,
                    coupon_percent, coupon_date, coupon_frequency, accrued_interest, coupon_period,
                    created, modified, uuid
                ) VALUES (
                    ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?,
                    ?, ?, ?
                )
                """;
    }

    @Override
    protected String getUpdateSql() {
        return """
                UPDATE exchange_security SET
                    sec_id = ?,
                    name = ?,
                    short_name = ?,
                    isin = ?,
                    reg_number = ?,
                    face_value = ?,
                    issue_date = ?,
                    mat_date = ?,
                    days_to_redemption = ?,
                    group_type = ?,
                    group_name = ?,
                    type = ?,
                    type_name = ?,
                    market_value = ?,
                    coupon_value = ?,
                    coupon_percent = ?,
                    coupon_date = ?,
                    coupon_frequency = ?,
                    accrued_interest = ?,
                    coupon_period = ?,
                    created = ?,
                    modified = ?
                WHERE UUID = ?
                """;
    }

    @Override
    protected ExchangeSecurity fromResultSet(ResultSet rs) throws SQLException {
        return new ExchangeSecurity(
                getUuid(rs, "uuid"),
                rs.getString("sec_id"),
                rs.getString("name"),
                rs.getString("short_name"),
                rs.getString("isin"),
                rs.getString("reg_number"),
                rs.getBigDecimal("face_value"),
                getLocalDate(rs, "issue_date"),
                getLocalDate(rs, "mat_date"),
                getInteger(rs, "days_to_redemption"),
                rs.getString("group_type"),
                rs.getString("group_name"),
                rs.getString("type"),
                rs.getString("type_name"),
                rs.getBigDecimal("market_value"),
                rs.getBigDecimal("coupon_value"),
                rs.getBigDecimal("coupon_percent"),
                getLocalDate(rs, "coupon_date"),
                getInteger(rs, "coupon_frequency"),
                rs.getBigDecimal("accrued_interest"),
                getInteger(rs, "coupon_period"),
                rs.getLong("created"),
                rs.getLong("modified")
        );
    }

    @Override
    protected void toStatement(PreparedStatement st, ExchangeSecurity security) throws SQLException {
        var index = 1;
        st.setString(index++, security.secId());
        st.setString(index++, security.name());
        st.setString(index++, security.shortName());
        st.setString(index++, security.isin());
        st.setString(index++, security.regNumber());
        st.setBigDecimal(index++, security.faceValue());
        setLocalDate(st, index++, security.issueDate());
        setLocalDate(st, index++, security.matDate());
        setInteger(st, index++, security.daysToRedemption());
        st.setString(index++, security.group());
        st.setString(index++, security.groupName());
        st.setString(index++, security.type());
        st.setString(index++, security.typeName());
        st.setBigDecimal(index++, security.marketValue());
        st.setBigDecimal(index++, security.couponValue());
        st.setBigDecimal(index++, security.couponPercent());
        setLocalDate(st, index++, security.couponDate());
        setInteger(st, index++, security.couponFrequency());
        st.setBigDecimal(index++, security.accruedInterest());
        setInteger(st, index++, security.couponPeriod());
        st.setLong(index++, security.created());
        st.setLong(index++, security.modified());
        setUuid(st, index, security.uuid());
    }
}
