package ee.sepp.urmas.paymentgateway.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record PaymentNotification(
        @NotBlank String message_id,
        @NotBlank String sender,
        @NotBlank String text,
        @NotBlank @Pattern(regexp = "\\d+", message = "Number expected") String receiver,
        @NotBlank String operator,
        @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timestamp
) {}
