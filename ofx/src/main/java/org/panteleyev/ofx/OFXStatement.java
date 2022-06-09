/*
 Copyright © 2020 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.ofx;

import java.util.List;

public class OFXStatement {
    private final Header header;

    // BankResponseMessageSetV1
    private final List<CreditCardStatementList> creditCardStatements;

    // CreditcardResponseMessageSetV1
    private final List<AccountStatementList> accountStatements;

    OFXStatement(Header header,
                 List<AccountStatementList> accountStatements,
                 List<CreditCardStatementList> creditCardStatements) {
        this.header = header;
        this.accountStatements = accountStatements;
        this.creditCardStatements = creditCardStatements;
    }

    public Header getHeader() {
        return header;
    }

    public List<CreditCardStatementList> getCreditCardStatements() {
        return creditCardStatements;
    }

    public List<AccountStatementList> getAccountStatements() {
        return accountStatements;
    }
}
