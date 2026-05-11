package io.github.rspereiratech.openapi.collection.generator.insomnia.deprecated;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InsomniaDeprecationMarkerTest {

    private final InsomniaDeprecationMarker marker = new InsomniaDeprecationMarker();

    @Test
    void markName_returnsInputUnchanged_whenNotDeprecated() {
        assertEquals("Get pet", marker.markName("Get pet", false));
    }

    @Test
    void markName_prependsWarningAndSuffix_whenDeprecated() {
        assertEquals("⚠ Get pet (deprecated)", marker.markName("Get pet", true));
    }

    @Test
    void markDescription_returnsInputUnchanged_whenNotDeprecated() {
        assertEquals("desc", marker.markDescription("desc", false));
    }

    @Test
    void markDescription_returnsOnlyWarning_whenDeprecatedAndDescriptionBlank() {
        assertEquals(
            "DEPRECATED: This operation may be removed in a future version.",
            marker.markDescription("", true));
    }

    @Test
    void markDescription_prependsWarningWithBlankLine_whenDeprecatedAndDescriptionPresent() {
        assertEquals(
            "DEPRECATED: This operation may be removed in a future version.\n\nExisting description",
            marker.markDescription("Existing description", true));
    }
}
