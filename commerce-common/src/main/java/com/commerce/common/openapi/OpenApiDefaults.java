package com.commerce.common.openapi;

import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;

import com.commerce.common.error.ApiError;
import com.commerce.common.openapi.ApiExamples.Errors;
import com.commerce.common.paging.PaginationProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

/**
 * Applies the documentation every endpoint shares: the generic 400 and 500 error responses, the {@link ApiError}
 * schema, and the paging parameter descriptions (taken from the live pagination configuration).
 */
public class OpenApiDefaults implements OperationCustomizer, OpenApiCustomizer {

    private static final String JSON = "application/json";

    private final PaginationProperties pagination;

    public OpenApiDefaults(PaginationProperties pagination) {
        this.pagination = pagination;
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        describePaging(operation.getParameters());
        if (operation.getResponses() == null) {
            operation.setResponses(new ApiResponses());
        }
        boolean hasInput = operation.getRequestBody() != null
                || (operation.getParameters() != null && !operation.getParameters().isEmpty());
        if (hasInput) {
            addError(operation.getResponses(), "400", "Validation failed or the request is malformed",
                    Errors.VALIDATION_FAILED);
        }
        addError(operation.getResponses(), "500", "Unexpected server error", Errors.INTERNAL_ERROR);
        return operation;
    }

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }
        var components = openApi.getComponents();
        var resolved = ModelConverters.getInstance().resolveAsResolvedSchema(new AnnotatedType(ApiError.class));
        resolved.referencedSchemas.forEach((name, schema) -> {
            if (components.getSchemas() == null || !components.getSchemas().containsKey(name)) {
                components.addSchemas(name, schema);
            }
        });
    }

    private void describePaging(List<Parameter> parameters) {
        if (parameters == null) {
            return;
        }
        for (var parameter : parameters) {
            if (!"query".equals(parameter.getIn()) || parameter.getDescription() != null) {
                continue;
            }
            switch (parameter.getName()) {
                case "page" -> {
                    parameter.setDescription("Zero-based page index; negative values are treated as 0");
                    parameter.setExample(0);
                }
                case "size" -> {
                    parameter.setDescription("Items per page; defaults to %d and is clamped to the range 1-%d"
                            .formatted(pagination.defaultSize(), pagination.maxSize()));
                    parameter.setExample(pagination.defaultSize());
                }
                default -> { }
            }
        }
    }

    private static void addError(ApiResponses responses, String status, String description, String example) {
        responses.computeIfAbsent(status, s -> new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(JSON, new MediaType()
                        .schema(new Schema<ApiError>().$ref(Components.COMPONENTS_SCHEMAS_REF + "ApiError"))
                        .addExamples("error", new Example().value(parse(example))))));
    }

    private static JsonNode parse(String json) {
        try {
            return Json.mapper().readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid OpenAPI example: " + json, e);
        }
    }
}
