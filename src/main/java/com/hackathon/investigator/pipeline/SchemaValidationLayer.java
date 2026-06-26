package com.hackathon.investigator.pipeline;

import com.hackathon.investigator.dto.AnalyzeTicketRequest;
import com.hackathon.investigator.dto.ErrorResponse;
import com.hackathon.investigator.dto.TransactionHistoryDto;
import com.hackathon.investigator.exception.SchemaValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SchemaValidationLayer {

    private final Validator validator;

    public void validate(AnalyzeTicketRequest request) {
        List<ErrorResponse.FieldErrorDetail> fieldErrors = new ArrayList<>();

        validator.validate(request).stream()
                .map(this::toFieldError)
                .forEach(fieldErrors::add);

        if (request.transactionHistory() != null) {
            for (int i = 0; i < request.transactionHistory().size(); i++) {
                TransactionHistoryDto transaction = request.transactionHistory().get(i);
                int index = i;
                validator.validate(transaction).stream()
                        .map(v -> toIndexedFieldError(index, v))
                        .forEach(fieldErrors::add);
            }
        }

        if (fieldErrors.isEmpty()) {
            return;
        }

        throw new SchemaValidationException(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Schema validation failed",
                "/analyze-ticket",
                Instant.now(),
                fieldErrors
        ));
    }

    private ErrorResponse.FieldErrorDetail toFieldError(ConstraintViolation<AnalyzeTicketRequest> violation) {
        return new ErrorResponse.FieldErrorDetail(
                violation.getPropertyPath().toString(),
                violation.getMessage(),
                violation.getInvalidValue()
        );
    }

    private ErrorResponse.FieldErrorDetail toIndexedFieldError(
            int index,
            ConstraintViolation<TransactionHistoryDto> violation
    ) {
        return new ErrorResponse.FieldErrorDetail(
                "transaction_history[" + index + "]." + violation.getPropertyPath(),
                violation.getMessage(),
                violation.getInvalidValue()
        );
    }
}
