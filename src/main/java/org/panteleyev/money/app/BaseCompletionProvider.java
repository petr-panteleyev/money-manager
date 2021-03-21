/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
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
