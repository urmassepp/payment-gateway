package ee.sepp.urmas.paymentgateway.notification;

import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentNotificationHandler {

    private final MerchantNotificationSender merchantNotificationSender;
    private final PaymentNotificationReplySender replySender;

    public void handle(PaymentNotification paymentNotification) {
        var merchantResponse = notifyMerchant(paymentNotification);
        sendReplyToProvider(merchantResponse, paymentNotification);
    }

    private MerchantReply notifyMerchant(PaymentNotification paymentNotification) {
        MerchantNotification merchantNotification = MerchantNotification.fromPaymentNotification(paymentNotification);
        log.info("Forwarding notification {} to merchant using transaction_id {}", paymentNotification.message_id(), merchantNotification.transaction_id);
        return merchantNotificationSender.send(merchantNotification);
    }

    private void sendReplyToProvider(MerchantReply merchantReply, PaymentNotification paymentNotification) {
        log.info("Sending reply to notification {}: {}", paymentNotification.message_id(), merchantReply.reply_message);
        try {
            replySender.send(createNotificationReplyParameters(merchantReply, paymentNotification));
            log.info("Notification {} successfully processed", paymentNotification.message_id());
        } catch (RuntimeException e) {
            log.error("Sending reply to {} failed: {}", paymentNotification.message_id(), e.getMessage());
        }
    }

    private Map<String,?> createNotificationReplyParameters(MerchantReply reply, PaymentNotification paymentNotification) {
        return Map.of("message", reply.reply_message,
                      "mo_message_id", paymentNotification.message_id(),
                      "receiver", paymentNotification.sender(),
                      "operator", paymentNotification.operator());
    }

    record MerchantNotification(String shortcode, String keyword, String message, String operator, String sender, String transaction_id){
        static MerchantNotification fromPaymentNotification(PaymentNotification paymentNotification) {
            return new MerchantNotification(paymentNotification.receiver(),
                                            extractKeyword(paymentNotification.text()),
                                            paymentNotification.text(),
                                            paymentNotification.operator(),
                                            paymentNotification.sender(),
                                            UUID.randomUUID().toString());
        }

        private static String extractKeyword(String text) {
            return text.split("[ +]")[0];
        }
    }

    record MerchantReply(String reply_message){}
}
