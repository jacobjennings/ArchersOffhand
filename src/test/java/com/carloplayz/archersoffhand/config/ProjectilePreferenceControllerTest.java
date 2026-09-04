package com.carloplayz.archersoffhand.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

class ProjectilePreferenceControllerTest {
    @Test
    void openingCurrentValueShowsEntireCatalogWhileTypingStillFilters() {
        AtomicReference<String> value = new AtomicReference<>(ProjectilePreferenceTokens.NORMAL_ARROW);
        Option<String> option = Option.<String>createBuilder()
                .name(Component.literal("Projectile"))
                .binding(ProjectilePreferenceTokens.NORMAL_ARROW, value::get, value::set)
                .controller(StringControllerBuilder::create)
                .build();

        Map<String, Component> labels = new LinkedHashMap<>();
        labels.put(ProjectilePreferenceTokens.NORMAL_ARROW, Component.literal("Normal Arrow"));
        labels.put(ProjectilePreferenceTokens.SPECTRAL_ARROW, Component.literal("Spectral Arrow"));
        labels.put(ProjectilePreferenceTokens.ANY_FIREWORK, Component.literal("Any firework rocket"));
        ProjectilePreferenceController controller = new ProjectilePreferenceController(option, labels);

        List<String> initialChoices = controller.matchingValues("Normal Arrow");
        assertEquals(3, initialChoices.size());
        assertEquals("Normal Arrow", initialChoices.getFirst());
        assertEquals(List.of("Spectral Arrow"), controller.matchingValues("spectral"));
        assertTrue(controller.matchingValues("firework").contains("Any firework rocket"));

        controller.setFromString("Spectral Arrow");
        assertEquals(ProjectilePreferenceTokens.SPECTRAL_ARROW, option.pendingValue());
    }
}
