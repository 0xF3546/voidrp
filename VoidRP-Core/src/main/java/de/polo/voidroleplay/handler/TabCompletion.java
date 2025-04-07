package de.polo.voidroleplay.handler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TabCompletion {

    private final String[] args;
    private final List<List<String>> suggestions;

    private TabCompletion(String[] args) {
        this.args = args;
        this.suggestions = new ObjectArrayList<>();
    }

    public static TabCompletion getBuilder(String[] args) {
        return new TabCompletion(args);
    }

    public TabCompletion addAtIndex(int index, List<String> options) {
        ensureSize(index);
        suggestions.set(index - 1, options);
        return this;
    }

    public TabCompletion addAtIndex(int index, String option) {
        return addAtIndex(index, Collections.singletonList(option));
    }

    public TabCompletion addFilteredAtIndex(int index, List<String> options, Predicate<String> filter) {
        ensureSize(index);
        suggestions.set(index - 1, options.stream().filter(filter).collect(Collectors.toList()));
        return this;
    }

    public List<String> build() {
        if (args.length == 0) return suggestions.get(0);

        int index = args.length - 1;
        if (index < suggestions.size()) {
            List<String> currentSuggestions = suggestions.get(index);
            String input = args[index];

            return currentSuggestions.stream()
                    .filter(suggestion -> suggestion.startsWith(input))
                    .collect(Collectors.toList());
        }

        return new ObjectArrayList<>();
    }

    private void ensureSize(int index) {
        while (suggestions.size() < index) {
            suggestions.add(new ObjectArrayList<>());
        }
    }
}
