package com.github.rspereiratech.openapi.collection.generator.insomnia.model;

/**
 * Represents a query parameter in an Insomnia request.
 *
 * @param name        the parameter name
 * @param value       the parameter value
 * @param description a human-readable description of the parameter
 */
public record InsomniaParameter(String name, String value, String description) {}
