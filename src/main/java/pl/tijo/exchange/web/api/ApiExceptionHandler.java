package pl.tijo.exchange.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.tijo.exchange.service.ExchangeException;
import pl.tijo.exchange.service.NotFoundException;

@RestControllerAdvice(assignableTypes = ApiController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(ExchangeException.class)
    public ProblemDetail handleRuleViolation(ExchangeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
