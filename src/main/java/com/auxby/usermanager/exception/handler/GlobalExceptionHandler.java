package com.auxby.usermanager.exception.handler;

import com.auxby.usermanager.exception.ActionNotAllowException;
import com.auxby.usermanager.exception.RegistrationException;
import com.auxby.usermanager.exception.SignInException;
import com.auxby.usermanager.exception.UserEmailNotValidatedException;
import com.auxby.usermanager.exception.response.ExceptionResponse;
import com.auxby.usermanager.utils.enums.CustomHttpStatus;
import org.postgresql.util.PSQLException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.auxby.usermanager.utils.enums.CustomHttpStatus.USER_EMAIL_NOT_VALIDATED;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        Map<String, List<String>> body = new HashMap<>();
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    protected ResponseEntity<ExceptionResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(value = RegistrationException.class)
    protected ResponseEntity<ExceptionResponse> handleRegistrationException(RegistrationException ex) {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                .body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(value = SignInException.class)
    protected ResponseEntity<ExceptionResponse> handleSignInException(SignInException ex) {
        return ResponseEntity.status(CustomHttpStatus.BAD_CREDENTIALS.getCode())
                .body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(value = UserEmailNotValidatedException.class)
    protected ResponseEntity<ExceptionResponse> handleUserEmailNotValidated(UserEmailNotValidatedException ex) {
        return ResponseEntity.status(USER_EMAIL_NOT_VALIDATED.getCode())
                .body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(value = PSQLException.class)
    protected ResponseEntity<ExceptionResponse> handleSQLException(PSQLException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(ex.getMessage()));
    }

    @ExceptionHandler(value = ActionNotAllowException.class)
    protected ResponseEntity<ExceptionResponse> handleEditNotAllow(ActionNotAllowException ex) {
        return ResponseEntity.status(CustomHttpStatus.ACTION_NOT_ALLOW.getCode())
                .body(new ExceptionResponse(ex.getMessage()));
    }
}
