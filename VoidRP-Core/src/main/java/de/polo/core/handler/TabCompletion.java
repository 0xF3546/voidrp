package de.polo.core.handler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TabCompletion {

    private final String[] args;
    private final List<Supplier<List<String>>> suggestions;

    private TabCompletion(String[] args) {
        this.args = args;
        this.suggestions = new ObjectArrayList<>();
    }

    public static TabCompletion getBuilder(String[] args) {
        return new TabCompletion(args);
    }

    public TabCompletion addAtIndex(int index, List<String> options) {
        return addAtIndex(index, () -> options);
    }

    public TabCompletion addAtIndex(int index, String option) {
        return addAtIndex(index, Collections.singletonList(option));
    }

    public TabCompletion addAtIndex(int index, Supplier<List<String>> supplier) {
        ensureSize(index);
        suggestions.set(index - 1, supplier);
        return this;
    }

    public TabCompletion addAtIndexIf(int targetIndex, int conditionIndex, String expectedValue, List<String> options) {
        return addAtIndex(targetIndex, () -> {
            if (args.length > conditionIndex && args[conditionIndex].equalsIgnoreCase(expectedValue)) {
                return options;
            }
            return Collections.emptyList();
        });
    }

    public TabCompletion addFilteredAtIndex(int index, List<String> options, Predicate<String> filter) {
        return addAtIndex(index, () -> options.stream().filter(filter).collect(Collectors.toList()));
    }

    public List<String> build() {
        if (args.length == 0) return getSuggestionsSafely(0);

        int index = args.length - 1;
        if (index < suggestions.size()) {
            List<String> currentSuggestions = getSuggestionsSafely(index);
            String input = args[index];
            return currentSuggestions.stream()
                    .filter(suggestion -> suggestion.startsWith(input))
                    .collect(Collectors.toList());
        }

        return new ObjectArrayList<>();
    }

    private void ensureSize(int index) {
        while (suggestions.size() < index) {
            suggestions.add(Collections::emptyList);
        }
    }

    private List<String> getSuggestionsSafely(int index) {
        try {
            Supplier<List<String>> supplier = suggestions.get(index);
            return supplier != null ? supplier.get() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
