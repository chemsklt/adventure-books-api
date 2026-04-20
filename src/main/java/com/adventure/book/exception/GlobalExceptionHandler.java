package com.adventure.book.exception;

import com.adventure.book.exception.book.BookNotFoundException;
import com.adventure.book.exception.book.InvalidBookException;
import com.adventure.book.exception.book.OptionNotFoundException;
import com.adventure.book.exception.book.SectionNotFoundException;
import com.adventure.book.exception.game.GameNotFoundException;
import com.adventure.book.generated.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({BookNotFoundException.class, SectionNotFoundException.class, OptionNotFoundException.class, GameNotFoundException.class})
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

    @ExceptionHandler({InvalidBookException.class, IllegalStateException.class})
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

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            org.springframework.web.HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }
}
