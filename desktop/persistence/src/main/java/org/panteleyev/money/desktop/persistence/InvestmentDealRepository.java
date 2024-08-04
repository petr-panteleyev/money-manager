/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.persistence;

import org.panteleyev.money.model.investment.InvestmentDeal;
import org.panteleyev.money.model.investment.InvestmentDealType;
import org.panteleyev.money.model.investment.InvestmentMarketType;
import org.panteleyev.money.model.investment.InvestmentOperationType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

final class InvestmentDealRepository extends Repository<InvestmentDeal> {
    InvestmentDealRepository() {
        super("investment_deal");
    }

    @Override
    protected String getInsertSql() {
        return """
                INSERT INTO investment_deal (
                    account_uuid
                    , security_uuid
                    , currency_uuid
                    , deal_number
                    , deal_date
                    , accounting_date
                    , market_type
                    , operation_type
                    , security_amount
                    , price
                    , aci
                    , deal_volume
                    , rate
                    , exchange_fee
                    , broker_fee
                    , amount
                    , deal_type
                    , created
                    , modified
                    , uuid
                ) VALUES (
                    ?
                    , ?
                    , ?
                    , ?
                    , ?
                    , ?
                    , ?
                    , ?
                    , ?
                    , ?
                    , ?
                    , ?
                    , ?
                    , ?
                    , ?
                    , ?
                    , ?
                    , ?
                    , ?
                    , ?
                ) ON CONFLICT (account_uuid, deal_number) DO UPDATE SET
                    security_uuid = EXCLUDED.security_uuid
                    , currency_uuid = EXCLUDED.currency_uuid
                    , amount = EXCLUDED.amount
                    , modified = EXCLUDED.modified
                """;
    }

    @Override
    protected String getUpdateSql() {
        return "";
    }

    @Override
    protected InvestmentDeal fromResultSet(ResultSet rs) throws SQLException {
        return new InvestmentDeal(
                rs.getObject("uuid", UUID.class),
                rs.getObject("account_uuid", UUID.class),
                rs.getObject("security_uuid", UUID.class),
                rs.getObject("currency_uuid", UUID.class),
                rs.getString("deal_number"),
                getLocalDateTime(rs, "deal_date"),
                getLocalDateTime(rs, "accounting_date"),
                InvestmentMarketType.valueOf(rs.getString("market_type")),
                InvestmentOperationType.valueOf(rs.getString("operation_type")),
                rs.getInt("security_amount"),
                rs.getBigDecimal("price"),
                rs.getBigDecimal("aci"),
                rs.getBigDecimal("deal_volume"),
                rs.getBigDecimal("rate"),
                rs.getBigDecimal("exchange_fee"),
                rs.getBigDecimal("broker_fee"),
                rs.getBigDecimal("amount"),
                InvestmentDealType.valueOf(rs.getString("deal_type")),
                rs.getLong("created"),
                rs.getLong("modified")
        );
    }

    @Override
    protected void toStatement(PreparedStatement st, InvestmentDeal investmentDeal) throws SQLException {
        var index = 1;
        st.setObject(index++, investmentDeal.accountUuid());
        st.setObject(index++, investmentDeal.securityUuid());
        st.setObject(index++, investmentDeal.currencyUuid());
        st.setString(index++, investmentDeal.dealNumber());
        st.setTimestamp(index++, Timestamp.valueOf(investmentDeal.dealDate()));
        st.setTimestamp(index++, Timestamp.valueOf(investmentDeal.accountingDate()));
        st.setString(index++, investmentDeal.marketType().name());
        st.setString(index++, investmentDeal.operationType().name());
        st.setInt(index++, investmentDeal.securityAmount());
        st.setBigDecimal(index++, investmentDeal.price());
        st.setBigDecimal(index++, investmentDeal.aci());
        st.setBigDecimal(index++, investmentDeal.dealVolume());
        st.setBigDecimal(index++, investmentDeal.rate());
        st.setBigDecimal(index++, investmentDeal.exchangeFee());
        st.setBigDecimal(index++, investmentDeal.brokerFee());
        st.setBigDecimal(index++, investmentDeal.amount());
        st.setString(index++, investmentDeal.dealType().name());
        st.setLong(index++, investmentDeal.created());
        st.setLong(index++, investmentDeal.modified());
        st.setObject(index, investmentDeal.uuid());
    }
}
