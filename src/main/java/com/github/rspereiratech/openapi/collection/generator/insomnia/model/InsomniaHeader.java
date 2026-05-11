package com.github.rspereiratech.openapi.collection.generator.insomnia.model;

/**
 * Represents a single HTTP header in an Insomnia request.
 *
 * @param name  the header name
 * @param value the header value
 */
public record InsomniaHeader(String name, String value) {}
