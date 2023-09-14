/*
 Copyright © 2020-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.ContactType;
import org.panteleyev.money.model.DocumentType;
import org.panteleyev.money.model.PeriodicPaymentType;
import org.panteleyev.money.model.RecurrenceType;
import org.panteleyev.money.model.TransactionType;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import static java.util.Map.entry;

public final class Bundles {
    private static final Map<CategoryType, String> CATEGORY_TYPE_STRINGS = new EnumMap<>(Map.of(
            CategoryType.BANKS_AND_CASH, "Банки",
            CategoryType.INCOMES, "Доходы",
            CategoryType.EXPENSES, "Расходы",
            CategoryType.DEBTS, "Долги",
            CategoryType.PORTFOLIO, "Портфель",
            CategoryType.ASSETS, "Активы",
            CategoryType.STARTUP, "Стартап"
    ));

    private static final Map<ContactType, String> CONTACT_TYPE_STRINGS = new EnumMap<>(Map.of(
            ContactType.PERSONAL, "Личное",
            ContactType.CLIENT, "Клиент",
            ContactType.SUPPLIER, "Поставщик",
            ContactType.EMPLOYEE, "Сотрудник",
            ContactType.EMPLOYER, "Работодатель",
            ContactType.SERVICE, "Услуга"
    ));

    private static final Map<TransactionType, String> TRANSACTION_TYPE_STRINGS = new EnumMap<>(Map.ofEntries(
            entry(TransactionType.CARD_PAYMENT, "Оплата по карте"),
            entry(TransactionType.SBP_PAYMENT, "Платеж через СБП"),
            entry(TransactionType.CASH_PURCHASE, "Покупка за наличные"),
            entry(TransactionType.CHEQUE, "Чек"),
            entry(TransactionType.TRANSFER, "Перевод"),
            entry(TransactionType.SBP_TRANSFER, "Перевод через СБП"),
            entry(TransactionType.DEPOSIT, "Депозит"),
            entry(TransactionType.WITHDRAWAL, "Снятие наличных"),
            entry(TransactionType.INTEREST, "Проценты"),
            entry(TransactionType.INCOME, "Доход"),
            entry(TransactionType.FEE, "Комиссия"),
            entry(TransactionType.CACHIER, "Транзакция в банкомате"),
            entry(TransactionType.DIVIDEND, "Дивиденды"),
            entry(TransactionType.DIRECT_BILLING, "Прямое дебетование"),
            entry(TransactionType.CHARGE, "Списание"),
            entry(TransactionType.PURCHASE, "Покупка"),
            entry(TransactionType.SALE, "Продажа"),
            entry(TransactionType.REFUND, "Возврат"),
            entry(TransactionType.UNDEFINED, "Неизвестно")
    ));

    private static final Map<DocumentType, String> DOCUMENT_TYPE_STRINGS = new EnumMap<>(Map.of(
            DocumentType.BILL, "Счет",
            DocumentType.RECEIPT, "Чек",
            DocumentType.CONTRACT, "Договор",
            DocumentType.STATEMENT, "Выписка",
            DocumentType.REPORT, "Отчёт",
            DocumentType.NOTIFICATION, "Уведомление",
            DocumentType.INVOICE, "Инвойс",
            DocumentType.PAYSLIP, "Платёжная ведомость",
            DocumentType.OTHER, "Другое"
    ));

    private static final Map<String, String> TRANSACTION_PREDICATE_STRINGS = Map.of(
            "ALL", "Все проводки",
            "CURRENT_MONTH", "Текущий месяц",
            "CURRENT_WEEK", "Текущая неделя",
            "CURRENT_YEAR", "Текущий год",
            "LAST_MONTH", "Последний месяц",
            "LAST_QUARTER", "Последний квартал",
            "LAST_YEAR", "Последний год"
    );

    private static final Map<PeriodicPaymentType, String> PERIODIC_PAYMENT_TYPE_STRINGS = new EnumMap<>(Map.of(
            PeriodicPaymentType.MANUAL_PAYMENT, "Ручной платёж",
            PeriodicPaymentType.AUTO_PAYMENT, "Автоплатеж",
            PeriodicPaymentType.CARD_PAYMENT, "Платёж с карты"
    ));

    private static final Map<RecurrenceType, String> RECURRENCE_TYPE_STRINGS = new EnumMap<>(Map.of(
            RecurrenceType.MONTHLY, "Раз в месяц",
            RecurrenceType.YEARLY, "Раз в год"
    ));

    public static String translate(CategoryType type) {
        return CATEGORY_TYPE_STRINGS.get(type);
    }

    public static String translate(ContactType type) {
        return CONTACT_TYPE_STRINGS.get(type);
    }

    public static String translate(TransactionType type) {
        return TRANSACTION_TYPE_STRINGS.get(type);
    }

    public static String translate(DocumentType type) {
        return DOCUMENT_TYPE_STRINGS.get(type);
    }

    public static String translate(TransactionPredicate predicate) {
        return TRANSACTION_PREDICATE_STRINGS.get(predicate.name());
    }

    public static String translate(PeriodicPaymentType type) {
        return PERIODIC_PAYMENT_TYPE_STRINGS.get(type);
    }

    public static String translate(RecurrenceType type) {
        return RECURRENCE_TYPE_STRINGS.get(type);
    }

    public static String translate(Month month) {
        return month == null ? "" : month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
    }

    private Bundles() {
    }
}
