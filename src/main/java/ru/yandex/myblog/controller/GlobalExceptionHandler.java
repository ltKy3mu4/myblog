package ru.yandex.myblog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.yandex.myblog.model.exception.CommentNotFoundException;
import ru.yandex.myblog.model.exception.ImageNotFoundException;
import ru.yandex.myblog.model.exception.PostNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ImageNotFoundException.class, CommentNotFoundException.class, PostNotFoundException.class})
    public ResponseEntity<String> handleImageNotFound(ImageNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<String> handleGeneralException(Exception ex) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
//    }
}