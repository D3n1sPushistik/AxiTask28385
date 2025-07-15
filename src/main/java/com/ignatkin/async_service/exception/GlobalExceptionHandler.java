package com.ignatkin.async_service.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<String> handleJsonProcessingException(JsonProcessingException ex) {
        logger.error("Ошибка обработки JSON: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body("Invalid JSON: " + ex.getOriginalMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        logger.error("Некорректный аргумент: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body("Invalid input: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOtherExceptions(Exception ex) {
        logger.error("Необработанное исключение: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body("Internal error: " + ex.getMessage());
    }
}
