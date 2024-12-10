package ai.learningsystems.gloobermkp.espproxies.proxy.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler
{
    
    // Default catch all exceptions
    @ExceptionHandler(Exception.class)
    public Flux<String> handleException(Exception ex) {
        // Log the exception
        log.error("Exception occurred: {}", ex.getMessage(), ex);
        
        // Return a Flux that emits the error message
        return Flux.just("GlobalExceptionHandler catch Exception: " + ex.getMessage() + " cause :" + ex.getCause())
                   .doOnTerminate(() -> log.info("Exception handling completed"));
    }

    //    @ExceptionHandler(Exception.class)        
//    public ResponseEntity<String> handleException(Exception ex) {
//        return new ResponseEntity<>("Exception: " + ex, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
}
