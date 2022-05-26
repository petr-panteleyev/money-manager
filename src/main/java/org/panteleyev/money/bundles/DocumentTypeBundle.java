/*
 Copyright (C) 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.bundles;

import java.util.ListResourceBundle;

import static org.panteleyev.money.model.DocumentType.BILL;
import static org.panteleyev.money.model.DocumentType.CONTRACT;
import static org.panteleyev.money.model.DocumentType.OTHER;
import static org.panteleyev.money.model.DocumentType.RECEIPT;
import static org.panteleyev.money.model.DocumentType.STATEMENT;

public class DocumentTypeBundle extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][]{
                {BILL.name(), "Bill"},
                {CONTRACT.name(), "Contract"},
                {RECEIPT.name(), "Receipt"},
                {STATEMENT.name(), "Statement"},
                {OTHER.name(), "Other"},
        };
    }
}
