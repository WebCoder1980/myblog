package org.myblog.users.exception;

import org.apache.logging.log4j.Logger;
import org.myblog.users.dto.AppResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.TreeMap;
import java.util.TreeSet;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @Autowired
    private Logger logger;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handle(MethodArgumentNotValidException ex) {
        AppResponse<?> result = new AppResponse<>(new TreeMap<>());

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(i -> {
                    result.getErrors().computeIfAbsent(i.getField(), j -> new TreeSet<>());
                    result.getErrors().get(i.getField()).add(i.getDefaultMessage());
                });

        logger.warn(String.format("Bad request: %s", result.toString()));

        return ResponseEntity.badRequest().body(result);
    }

    @ExceptionHandler(RestIllegalArgumentException.class)
    public ResponseEntity<?> handle(RestIllegalArgumentException ex) {
        logger.warn(String.format("Bad request: %s - %s", ex.getField(), ex.getMessage()));

        return ResponseEntity.badRequest().body(new AppResponse<>().addErrorFluent(ex.getField(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handle(Exception ex) {
        logger.warn(String.format("Bad request: %s", ex.getMessage()));

        return ResponseEntity.badRequest().body(new AppResponse<>().addErrorFluent(ex.getMessage()));
    }
}
