package ee.sepp.urmas.paymentgateway.notification;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentNotificationController {

    private final PaymentNotificationHandler paymentNotificationHandler;

    @GetMapping("/sms")
    public ResponseEntity<String> receivePaymentNotification(@Validated PaymentNotification paymentNotification) {
        log.info("Received {}", paymentNotification);
        forwardNotification(paymentNotification);
        return ResponseEntity.ok("OK");
    }

    private void forwardNotification(PaymentNotification paymentNotification) {
        CompletableFuture.runAsync(() -> paymentNotificationHandler.handle(paymentNotification));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private List<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return exception.getBindingResult().getFieldErrors().stream()
                .map(err -> String.format("%s - %s", err.getField(), err.getDefaultMessage())).toList();
    }
}
