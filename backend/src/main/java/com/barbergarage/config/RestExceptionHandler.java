package com.barbergarage.config;
import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import org.springframework.web.bind.MethodArgumentNotValidException;import java.util.*;
@RestControllerAdvice
public class RestExceptionHandler {
 @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class}) ResponseEntity<?> bad(RuntimeException e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> invalid(MethodArgumentNotValidException e){String m=e.getBindingResult().getFieldErrors().stream().findFirst().map(x->x.getField()+": "+x.getDefaultMessage()).orElse("Datos inválidos");return ResponseEntity.badRequest().body(Map.of("message",m));}
}
