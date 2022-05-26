/*
 Copyright (C) 2019, 2020, 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.app;

import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public abstract class BaseCompletionProvider<T> implements Callback<AutoCompletionBinding.ISuggestionRequest,
        Collection<T>> {
    private final Set<T> set;
    private final Supplier<Integer> minLengthSupplier;

    public BaseCompletionProvider(Set<T> set, Supplier<Integer> minLengthSupplier) {
        this.set = set;
        this.minLengthSupplier = minLengthSupplier;
    }

    public abstract String getElementString(T element);

    @Override
    public Collection<T> call(AutoCompletionBinding.ISuggestionRequest req) {
        if (req.getUserText().length() < minLengthSupplier.get()) {
            return List.of();
        }

        var userText = req.getUserText();
        var stripped = userText.stripLeading().toLowerCase();

        var result = set.stream()
                .filter(it -> getElementString(it).toLowerCase().contains(stripped))
                .toList();

        if (result.size() == 1 && getElementString(result.get(0)).equals(userText)) {
            /* If there is a single case sensitive match then no suggestions must be shown. */
            return List.of();
        } else {
            return result;
        }
    }
}
