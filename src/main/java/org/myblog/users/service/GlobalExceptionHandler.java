package org.myblog.users.service;

import org.myblog.users.dto.AppResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.TreeMap;
import java.util.TreeSet;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleError(MethodArgumentNotValidException ex) {
        AppResponse<?> result = new AppResponse<>(new TreeMap<>());

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(i -> {
                    result.getErrors().computeIfAbsent(i.getField(), j -> new TreeSet<>());
                    result.getErrors().get(i.getField()).add(i.getDefaultMessage());
                });

        return ResponseEntity.badRequest().body(result);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleError(Exception ex) {
        return ResponseEntity.badRequest().body(new AppResponse<>().addErrorFluent(ex.getMessage()));
    }
}
