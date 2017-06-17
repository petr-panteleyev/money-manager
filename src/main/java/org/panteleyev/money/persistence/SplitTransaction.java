/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.panteleyev.money.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SplitTransaction extends Transaction {
    private final String contactString;
    private final String accountCreditedString;

    public SplitTransaction(int id, List<Transaction> group) {
        super(group.get(0).getId(), calculateTotal(group),
                group.get(0).getDay(),
                group.get(0).getMonth(),
                group.get(0).getYear(),
                calculateTransactionType(group).getId(),
                calculateComment(group),
                false,
                group.get(0).getAccountDebitedId(), 0, CategoryType.BANKS_AND_CASH.getId(),CategoryType.BANKS_AND_CASH.getId(),
                0,
                0,
                id, 0, BigDecimal.ONE, 0, "");

        MoneyDAO dao = MoneyDAO.getInstance();

        contactString = group.stream()
                .map(Transaction::getContactId)
                .filter(x -> (x != null))
                .distinct()
                .map(dao::getContact)
                .filter(Optional::isPresent)
                .map(o -> o.get().getName())
                .collect(Collectors.joining(" + "));

        List<Integer> accCredIDs = group.stream()
                .map(Transaction::getAccountCreditedId)
                .filter(x -> (x != null))
                .distinct()
                .collect(Collectors.toList());

        if (accCredIDs.size() == 1) {
            accountCreditedString = dao.getAccount(accCredIDs.get(0))
                    .map(Account::getName)
                    .orElse("");
        } else {
            accountCreditedString = accCredIDs.size() + " accounts";
        }
    }

    public String getContactString() {
        return contactString;
    }

    public String getAccountCreditedString() {
        return accountCreditedString;
    }

    private static BigDecimal calculateTotal(List<Transaction> group) {
        return group.stream()
                .map(Transaction::getSignedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static TransactionType calculateTransactionType(List<Transaction> group) {
        return group.stream()
                .filter(t -> !t.getTransactionType().equals(TransactionType.UNDEFINED))
                .findFirst()
                .map(Transaction::getTransactionType)
                .orElse(TransactionType.UNDEFINED);
    }

    private static String calculateComment(List<Transaction> group) {
        return group.stream()
                .map(Transaction::getComment)
                .filter(c -> c != null && !c.isEmpty())
                .findFirst()
                .orElse("");
    }
}
