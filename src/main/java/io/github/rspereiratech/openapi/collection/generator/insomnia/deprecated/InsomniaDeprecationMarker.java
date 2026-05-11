package io.github.rspereiratech.openapi.collection.generator.insomnia.deprecated;

import io.github.rspereiratech.openapi.collection.generator.core.deprecated.DeprecationMarker;

/**
 * {@link DeprecationMarker} implementation that uses Insomnia-friendly formatting
 * with a warning emoji prefix and descriptive suffix for deprecated operations.
 */
public class InsomniaDeprecationMarker implements DeprecationMarker {

    @Override
    public String markName(String name, boolean deprecated) {
        return deprecated ? "⚠ " + name + " (deprecated)" : name;
    }

    @Override
    public String markDescription(String description, boolean deprecated) {
        if (!deprecated) {
            return description;
        }

        String warning = "DEPRECATED: This operation may be removed in a future version.";
        return description.isBlank() ? warning : warning + "\n\n" + description;
    }
}
