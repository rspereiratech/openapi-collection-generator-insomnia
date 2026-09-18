package io.github.rspereiratech.openapi.collection.generator.insomnia.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Top-level container representing a complete Insomnia export document.
 *
 * @param type       the export type identifier (always {@value #TYPE})
 * @param exportFormat  the export format version
 * @param exportDate    ISO-8601 timestamp of the export
 * @param exportSource  identifier of the tool that produced the export
 * @param resources     the collection of Insomnia resources included in the export
 */
public record InsomniaExport(
    @JsonProperty("_type") String type,
    @JsonProperty("__export_format") int exportFormat,
    @JsonProperty("__export_date") String exportDate,
    @JsonProperty("__export_source") String exportSource,
    List<InsomniaResource> resources) {

    /**
     * Insomnia export format version.
     */
    public static final int FORMAT_VERSION = 4;

    /**
     * Insomnia resource type identifier for exports.
     */
    public static final String TYPE = "export";

    /**
     * Source identifier written into the export metadata.
     */
    public static final String SOURCE = "openapi-collection-maven-plugin";

    /**
     * Fixed value written as {@code __export_date}.
     *
     * <p>Generated collections are build outputs that are typically committed, so a wall-clock
     * timestamp here made every build produce a different file. Insomnia does not act on this
     * field, so it is pinned to the Unix epoch to keep exports reproducible.
     */
    public static final String EXPORT_DATE = "1970-01-01T00:00:00.000Z";
}
