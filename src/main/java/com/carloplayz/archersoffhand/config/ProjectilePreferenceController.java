package com.carloplayz.archersoffhand.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.dropdown.DropdownStringController;
import dev.isxander.yacl3.gui.controllers.dropdown.DropdownStringControllerElement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.chat.Component;

/** Searchable human-readable dropdown that persists stable preference tokens. */
final class ProjectilePreferenceController extends DropdownStringController {
    private final Map<String, String> tokenToLabel;
    private final Map<String, String> labelToToken;

    ProjectilePreferenceController(Option<String> option, Map<String, Component> labels) {
        this(option, choices(labels));
    }

    private ProjectilePreferenceController(Option<String> option, Choices choices) {
        super(option, choices.labels(), false, false);
        this.tokenToLabel = choices.tokenToLabel();
        this.labelToToken = choices.labelToToken();
    }

    @Override
    public String getString() {
        String token = option().pendingValue();
        return tokenToLabel.getOrDefault(token, token);
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> dimension) {
        return new DropdownStringControllerElement(this, screen, dimension) {
            @Override
            public List<String> computeMatchingValues() {
                return ProjectilePreferenceController.this.matchingValues(inputField);
            }
        };
    }

    @Override
    public void setFromString(String value) {
        String selectedLabel = getValidValue(value);
        String currentToken = option().pendingValue();
        option().requestSet(labelToToken.getOrDefault(selectedLabel, currentToken));
    }

    List<String> matchingValues(String input) {
        // YACL seeds the search box with the current display value. Treat that
        // untouched value as an empty query so opening the dropdown shows the
        // complete catalog instead of filtering it to the current choice.
        String query = input.equals(getString()) ? "" : input.toLowerCase(Locale.ROOT);
        return getAllowedValues(input).stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator
                        .comparing((String value) -> !value.toLowerCase(Locale.ROOT).startsWith(query))
                        .thenComparing(String::compareToIgnoreCase))
                .toList();
    }

    private static Choices choices(Map<String, Component> entries) {
        Map<String, String> tokenToLabel = new LinkedHashMap<>();
        Map<String, String> labelToToken = new LinkedHashMap<>();
        List<String> labels = new ArrayList<>();
        Map<String, Integer> occurrences = new LinkedHashMap<>();

        entries.forEach((token, component) -> {
            String base = component.getString();
            int occurrence = occurrences.merge(base, 1, Integer::sum);
            String label = occurrence == 1 ? base : base + " [variant " + occurrence + "]";
            tokenToLabel.put(token, label);
            labelToToken.put(label, token);
            labels.add(label);
        });
        return new Choices(List.copyOf(labels), Map.copyOf(tokenToLabel), Map.copyOf(labelToToken));
    }

    private record Choices(List<String> labels, Map<String, String> tokenToLabel, Map<String, String> labelToToken) {
    }
}
