/*
 Copyright © 2021-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getEnum;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getUuid;

@Repository
public class TransactionRepository implements MoneyRepository<Transaction> {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<Transaction> rowMapper = (rs, _) -> new Transaction(
            getUuid(rs, "uuid"),
            rs.getBigDecimal("amount"),
            rs.getBigDecimal("credit_amount"),
            rs.getDate("transaction_date").toLocalDate(),
            getEnum(rs, "type", TransactionType.class),
            rs.getString("comment"),
            rs.getBoolean("checked"),
            getUuid(rs, "acc_debited_uuid"),
            getUuid(rs, "acc_credited_uuid"),
            getEnum(rs, "acc_debited_type", CategoryType.class),
            getEnum(rs, "acc_credited_type", CategoryType.class),
            getUuid(rs, "acc_debited_category_uuid"),
            getUuid(rs, "acc_credited_category_uuid"),
            getUuid(rs, "contact_uuid"),
            rs.getString("invoice_number"),
            getUuid(rs, "parent_uuid"),
            rs.getBoolean("detailed"),
            rs.getDate("statement_date").toLocalDate(),
            getUuid(rs, "card_uuid"),
            rs.getLong("created"),
            rs.getLong("modified")
    );

    private static Map<String, Object> toMap(Transaction transaction) {
        var map = new HashMap<String, Object>(Map.ofEntries(
                entry("uuid", transaction.uuid()),
                entry("amount", transaction.amount()),
                entry("creditAmount", transaction.creditAmount()),
                entry("transactionDate", Date.valueOf(transaction.transactionDate())),
                entry("type", transaction.type().name()),
                entry("comment", transaction.comment()),
                entry("checked", transaction.checked()),
                entry("accDebitedUuid", transaction.accountDebitedUuid()),
                entry("accCreditedUuid", transaction.accountCreditedUuid()),
                entry("accDebitedType", transaction.accountDebitedType().name()),
                entry("accCreditedType", transaction.accountCreditedType().name()),
                entry("accDebitedCategoryUuid", transaction.accountDebitedCategoryUuid()),
                entry("accCreditedCategoryUuid", transaction.accountCreditedCategoryUuid()),
                entry("invoiceNumber", transaction.invoiceNumber()),
                entry("detailed", transaction.detailed()),
                entry("created", transaction.created()),
                entry("modified", transaction.modified())
        ));
        map.put("contactUuid", transaction.contactUuid());
        map.put("parentUuid", transaction.parentUuid());
        map.put("statementDate", Date.valueOf(transaction.statementDate()));
        map.put("cardUuid", transaction.cardUuid());
        return map;
    }

    public TransactionRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Transaction> getAll() {
        return jdbcTemplate.query("SELECT * FROM transaction", rowMapper);
    }

    public List<Transaction> getByYearAndMonth(int year, int month) {
        return jdbcTemplate.query("""
                        SELECT * FROM transaction
                        WHERE date_month = :month AND date_year = :year
                        """,
                Map.of(
                        "month", month,
                        "year", year
                ),
                rowMapper);
    }

    public Collection<Transaction> getByAccountId(UUID uuid) {
        return jdbcTemplate.query("""
                        SELECT * FROM transaction
                        WHERE acc_debited_uuid = :uuid OR acc_credited_uuid = :uuid
                        """,
                Map.of(
                        "uuid", uuid
                ),
                rowMapper);
    }

    @Override
    public Stream<Transaction> getStream() {
        return jdbcTemplate.queryForStream("SELECT * FROM transaction", Map.of(), rowMapper);
    }

    @Override
    public Optional<Transaction> get(UUID uuid) {
        var result = jdbcTemplate.query("""
                        SELECT * FROM transaction WHERE uuid = :uuid
                        """,
                Map.of("uuid", uuid),
                rowMapper);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    @Override
    public int insertOrUpdate(Transaction transaction) {
        return jdbcTemplate.update("""
                        INSERT INTO transaction (
                            uuid, amount, credit_amount, transaction_date, type, comment, checked,
                            acc_debited_uuid, acc_credited_uuid, acc_debited_type, acc_credited_type,
                            acc_debited_category_uuid, acc_credited_category_uuid, contact_uuid,
                            invoice_number, parent_uuid, detailed, statement_date, card_uuid, created, modified
                        ) VALUES (
                            :uuid, :amount, :creditAmount, :transactionDate, :type, :comment, :checked,
                            :accDebitedUuid, :accCreditedUuid, :accDebitedType, :accCreditedType,
                            :accDebitedCategoryUuid, :accCreditedCategoryUuid, :contactUuid,
                            :invoiceNumber, :parentUuid, :detailed, :statementDate, :cardUuid, :created, :modified
                        )
                        ON CONFLICT (uuid) DO UPDATE SET
                            uuid = :uuid,
                            amount = :amount,
                            credit_amount = :creditAmount,
                            transaction_date = :transactionDate,
                            type = :type,
                            comment = :comment,
                            checked = :checked,
                            acc_debited_uuid = :accDebitedUuid,
                            acc_credited_uuid = :accCreditedUuid,
                            acc_debited_type = :accDebitedType,
                            acc_credited_type = :accCreditedType,
                            acc_debited_category_uuid = :accDebitedCategoryUuid,
                            acc_credited_category_uuid = :accCreditedCategoryUuid,
                            contact_uuid = :contactUuid,
                            invoice_number = :invoiceNumber,
                            parent_uuid = :parentUuid,
                            detailed = :detailed,
                            statement_date = :statementDate,
                            card_uuid = :cardUuid,
                            modified = :modified
                        """,
                toMap(transaction)
        );
    }
}
