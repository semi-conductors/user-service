package com.rentmate.service.user.service.shared.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandlers {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandlers.class);

    @ExceptionHandler(RegistrationException.class)
    public ProblemDetail handleRegistrationException(RegistrationException exception) {
        var result = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        result.setTitle("Registration failed");
        result.setDetail(exception.getMessage());
        return result;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        pd.setTitle("Validation failed");
        pd.setDetail("One or more fields are invalid.");
        pd.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
                .toList());

        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleServerException(Exception ex) {
        logger.error("internal exception: ",ex);
        var result = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        result.setTitle("Internal server error");
        result.setDetail(ex.getMessage());
        return ResponseEntity.internalServerError().body(result);
    }

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleSessionException(SessionNotFoundException ex) {
        var result = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        result.setTitle("Session not found");
        result.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentialsException(BadCredentialsException ex) {
        var result = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        result.setTitle("Bad credentials");
        result.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFoundException(NotFoundException ex) {
        var result = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        result.setTitle("Not found");
        result.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ProblemDetail> handleBadRequestException(BadRequestException ex) {
        var result = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        result.setTitle("Bad request");
        result.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleInvalidEnum(HttpMessageNotReadableException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Bad request");
        pd.setDetail("Invalid input value, if there this is an enum, make sure of using correct values.");
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException ex) {
        var result = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        result.setTitle("Access denied");
        result.setDetail("You do not have permission to access this resource.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }
}
