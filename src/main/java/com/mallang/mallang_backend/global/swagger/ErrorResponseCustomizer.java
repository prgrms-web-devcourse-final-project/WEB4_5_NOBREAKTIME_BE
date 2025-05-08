package com.mallang.mallang_backend.global.swagger;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ErrorResponse;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.exception.message.MessageService;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ErrorResponseCustomizer implements OperationCustomizer {

    private final MessageService messageService;
    private final HttpServletRequest request;

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        PossibleErrors annotation = handlerMethod.getMethodAnnotation(PossibleErrors.class);
        if (annotation == null) return operation;

        Arrays.stream(annotation.value())
                .collect(Collectors.groupingBy(ErrorCode::getStatus))
                .forEach((status, codes) -> {
                    String responseCode = String.valueOf(status.value());
                    Content content = createContent(codes);
                    operation.getResponses().addApiResponse(responseCode,
                            new ApiResponse()
                                    .description(status.getReasonPhrase())
                                    .content(content)
                    );
                });

        return operation;
    }

    private Content createContent(List<ErrorCode> codes) {
        MediaType mediaType = new MediaType();
        codes.forEach(code -> {
            mediaType.addExamples(code.name(),
                    new Example().value(createExampleValue(code, request))
            );
        });
        return new Content().addMediaType("application/json", mediaType);
    }

    private Map<String, Object> createExampleValue(ErrorCode code, HttpServletRequest request) {
        ServiceException simulatedException = new ServiceException(code);
        ErrorResponse response = ErrorResponse.of(
            simulatedException,
            request,
            messageService // MessageService 주입 필요
        );

        Map<String, Object> errorResponseExample = new LinkedHashMap<>();
        errorResponseExample.put("timestamp", response.getTimestamp().toString());
        errorResponseExample.put("status", response.getStatus());
        errorResponseExample.put("code", response.getCode());
        errorResponseExample.put("message", response.getMessage());
        errorResponseExample.put("errors", response.getErrors());
        errorResponseExample.put("path", "실제 메서드 경로");

        return errorResponseExample ;
    }
}
