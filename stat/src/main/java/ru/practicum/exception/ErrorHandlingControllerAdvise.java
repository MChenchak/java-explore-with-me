package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class ErrorHandlingControllerAdvise {

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ConstraintViolationException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<Object> handleError(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

}
