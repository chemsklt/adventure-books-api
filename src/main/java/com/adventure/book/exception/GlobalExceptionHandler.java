package com.adventure.book.exception;

import com.adventure.book.generated.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({BookNotFoundException.class, SectionNotFoundException.class, OptionNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException  ex, HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidBookException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBook(RuntimeException  ex, HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse()
                .timestamp(java.time.OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI());

        return ResponseEntity.badRequest().body(error);
    }
}
