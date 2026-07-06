// Copyright © 2024-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.investment;

import org.panteleyev.money.model.ExchangeSecuritySplit;
import org.panteleyev.money.model.InvestmentDeal;

import java.util.Comparator;

public class DealAnsSplitComparator implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof InvestmentDeal deal1 && o2 instanceof InvestmentDeal deal2) {
            return deal1.dealDate().compareTo(deal2.dealDate());
        }

        if (o1 instanceof ExchangeSecuritySplit s1 && o2 instanceof ExchangeSecuritySplit s2) {
            return s1.date().compareTo(s2.date());
        }

        if (o1 instanceof InvestmentDeal deal && o2 instanceof ExchangeSecuritySplit split) {
            return deal.dealDate().toLocalDate().compareTo(split.date());
        }

        if (o1 instanceof ExchangeSecuritySplit split && o2 instanceof InvestmentDeal deal) {
            return split.date().compareTo(deal.dealDate().toLocalDate());
        }

        throw new IllegalStateException("Wrong object in comparator");
    }
}
