/*
 Copyright © 2021 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.panteleyev.money.backend.repository.RepositoryUtil.convert;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getBoolean;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getEnum;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getLocalDate;
import static org.panteleyev.money.backend.repository.RepositoryUtil.getUuid;

@Repository
public class TransactionRepository implements MoneyRepository<Transaction> {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<Transaction> rowMapper = (rs, i) -> new Transaction(
        getUuid(rs, "uuid"),
        rs.getBigDecimal("amount"),
        rs.getInt("date_day"),
        rs.getInt("date_month"),
        rs.getInt("date_year"),
        getEnum(rs, "type", TransactionType.class),
        rs.getString("comment"),
        getBoolean(rs, "checked"),
        getUuid(rs, "acc_debited_uuid"),
        getUuid(rs, "acc_credited_uuid"),
        getEnum(rs, "acc_debited_type", CategoryType.class),
        getEnum(rs, "acc_credited_type", CategoryType.class),
        getUuid(rs, "acc_debited_category_uuid"),
        getUuid(rs, "acc_credited_category_uuid"),
        getUuid(rs, "contact_uuid"),
        rs.getBigDecimal("rate"),
        rs.getInt("rate_direction"),
        rs.getString("invoice_number"),
        getUuid(rs, "parent_uuid"),
        getBoolean(rs, "detailed"),
        getLocalDate(rs, "statement_date"),
        rs.getLong("created"),
        rs.getLong("modified")
    );

    private static Map<String, Object> toMap(Transaction transaction) {
        var map = new HashMap<String, Object>(Map.ofEntries(
            entry("uuid", transaction.uuid().toString()),
            entry("amount", transaction.amount()),
            entry("day", transaction.day()),
            entry("month", transaction.month()),
            entry("year", transaction.year()),
            entry("type", transaction.type().name()),
            entry("comment", transaction.comment()),
            entry("checked", convert(transaction.checked())),
            entry("accDebitedUuid", transaction.accountDebitedUuid().toString()),
            entry("accCreditedUuid", transaction.accountCreditedUuid().toString()),
            entry("accDebitedType", transaction.accountDebitedType().name()),
            entry("accCreditedType", transaction.accountCreditedType().name()),
            entry("accDebitedCategoryUuid", transaction.accountDebitedCategoryUuid().toString()),
            entry("accCreditedCategoryUuid", transaction.accountCreditedCategoryUuid().toString()),
            entry("rate", transaction.rate()),
            entry("rateDirection", transaction.rateDirection()),
            entry("invoiceNumber", transaction.invoiceNumber()),
            entry("detailed", convert(transaction.detailed())),
            entry("created", transaction.created()),
            entry("modified", transaction.modified())
        ));
        map.put("contactUuid", convert(transaction.contactUuid()));
        map.put("parentUuid", convert(transaction.parentUuid()));
        map.put("statementDate", convert(transaction.statementDate()));
        return map;
    }

    public TransactionRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Transaction> getAll() {
        return jdbcTemplate.query("SELECT * FROM transaction", rowMapper);
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
            Map.of("uuid", uuid.toString()),
            rowMapper);
        return result.size() == 0 ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public int insert(Transaction transaction) {
        return jdbcTemplate.update("""
                INSERT INTO transaction (
                    uuid, amount, date_day, date_month, date_year, type, comment, checked,
                    acc_debited_uuid, acc_credited_uuid, acc_debited_type, acc_credited_type,
                    acc_debited_category_uuid, acc_credited_category_uuid, contact_uuid, rate, rate_direction,
                    invoice_number, parent_uuid, detailed, statement_date, created, modified
                ) VALUES (
                    :uuid, :amount, :day, :month, :year, :type, :comment, :checked,
                    :accDebitedUuid, :accCreditedUuid, :accDebitedType, :accCreditedType,
                    :accDebitedCategoryUuid, :accCreditedCategoryUuid, :contactUuid, :rate, :rateDirection,
                    :invoiceNumber, :parentUuid, :detailed, :statementDate, :created, :modified
                )
                """,
            toMap(transaction)
        );
    }

    @Override
    public int update(Transaction transaction) {
        return jdbcTemplate.update("""
                UPDATE transaction SET
                    uuid = :uuid,
                    amount = :amount,
                    date_day = :day,
                    date_month = :month,
                    date_year = :year,
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
                    rate = :rate,
                    rate_direction = :rateDirection,
                    invoice_number = :invoiceNumber,
                    parent_uuid = :parentUuid,
                    detailed = :detailed,
                    statement_date = :statementDate,
                    modified = :modified
                WHERE uuid = :uuid
                """,
            toMap(transaction)
        );
    }
}
