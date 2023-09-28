package ee.sepp.urmas.paymentgateway.notification;

import ee.sepp.urmas.paymentgateway.configuration.PaymentGatewayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
class MerchantNotificationSender {

    private static final PaymentNotificationHandler.MerchantReply FAILURE_MESSAGE = new PaymentNotificationHandler.MerchantReply(
            "Something went wrong. Please contact us at cs.boku.com to receive your service");

    private final PaymentGatewayProperties properties;
    private final WebClient merchantWebClient;

    public PaymentNotificationHandler.MerchantReply send(PaymentNotificationHandler.MerchantNotification merchantNotification) {
        try {
            var result = merchantWebClient
                    .post()
                    .uri(findUrl(merchantNotification.keyword()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(merchantNotification)
                    .retrieve()
                    .toEntity(PaymentNotificationHandler.MerchantReply.class)
                    .blockOptional();
            if (HttpStatus.OK.equals(result.map(ResponseEntity::getStatusCode).orElse(null))) {
                return result.get().getBody();
            }
        } catch (RuntimeException e) {
            log.error("Error when forwarding notification to merchant, transaction_id {}", merchantNotification.transaction_id(), e);
        }
        return FAILURE_MESSAGE;
    }

    private String findUrl(String keyword) {
        return switch (keyword) {
            case "TXT" -> properties.getMerchantEndpointTxt();
            case "FOR" -> properties.getMerchantEndpointFor();
            default -> throw new IllegalStateException("Unexpected keyword: " + keyword);
        };
    }
}
