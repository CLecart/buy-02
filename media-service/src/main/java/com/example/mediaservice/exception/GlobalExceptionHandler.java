package com.example.mediaservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "invalid_argument");
        body.put("message", ex.getMessage());
        return body;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "file_too_large");
        body.put("message", "File size exceeds maximum allowed size of 2MB");
        return body;
    }

    @ExceptionHandler(FileStorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, Object> handleFileStorage(FileStorageException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "internal_error");
        body.put("message", ex.getMessage());
        return body;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleValidation(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "invalid_argument");
        body.put("message", ex.getMessage());
        return body;
    }
}
