package ee.sepp.urmas.paymentgateway.notification;

import ee.sepp.urmas.paymentgateway.configuration.PaymentGatewayProperties;
import java.util.Map;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentNotificationReplySender {

    private final PaymentGatewayProperties properties;
    private final WebClient providerWebClient;

    @Retryable(maxAttempts = 5, backoff = @Backoff(multiplier = 2))
    public void send(Map<String, ?> parameters) {
        providerWebClient.get()
                .uri(uriBuilder -> {
                    parameters.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .onStatus(Predicate.not(HttpStatus.OK::equals),
                          response -> {
                              log.warn("Error when sending reply to {}: {}. Retrying.", parameters.get("mo_message_id"), response.statusCode());
                              throw new RuntimeException();
                })
                .bodyToMono(String.class)
                .blockOptional(properties.getTimeout());
    }
}
