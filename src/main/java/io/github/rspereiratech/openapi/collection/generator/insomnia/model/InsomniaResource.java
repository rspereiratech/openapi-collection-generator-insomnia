package io.github.rspereiratech.openapi.collection.generator.insomnia.model;

/**
 * Sealed interface representing a resource within an Insomnia collection export.
 *
 * <p>Permitted subtypes model the different resource kinds that Insomnia supports:
 * workspaces, environments, request groups (folders), and requests.</p>
 */
public sealed interface InsomniaResource permits InsomniaWorkspace, InsomniaEnvironment, InsomniaRequestGroup, InsomniaRequest {}
