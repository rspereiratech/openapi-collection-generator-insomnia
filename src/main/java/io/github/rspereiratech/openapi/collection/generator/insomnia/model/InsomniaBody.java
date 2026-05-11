package io.github.rspereiratech.openapi.collection.generator.insomnia.model;

/**
 * Represents the body of an Insomnia HTTP request.
 *
 * @param mimeType the MIME type of the body content (e.g. "application/json")
 * @param text     the serialized body content
 */
public record InsomniaBody(String mimeType, String text) {}
