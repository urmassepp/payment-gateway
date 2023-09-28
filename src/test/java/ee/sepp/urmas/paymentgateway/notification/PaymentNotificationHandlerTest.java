package ee.sepp.urmas.paymentgateway.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentNotificationHandlerTest {

    @InjectMocks
    private PaymentNotificationHandler notificationHandler;

    @Mock
    private MerchantNotificationSender merchantNotificationSender;
    @Mock
    private PaymentNotificationReplySender replySender;

    @Captor
    private ArgumentCaptor<PaymentNotificationHandler.MerchantNotification> merchantNotificationCaptor;
    @Captor
    private ArgumentCaptor<Map<String, ?>> replyCaptor;

    @Test
    void sendsTXTNotificationToMerchant() {
        var paymentNotification = createPaymentNotification("TXT COINS");
        when(merchantNotificationSender.send(any())).thenReturn(new PaymentNotificationHandler.MerchantReply("test passed"));

        notificationHandler.handle(paymentNotification);

        verify(merchantNotificationSender).send(merchantNotificationCaptor.capture());
        assertThat(merchantNotificationCaptor.getValue().shortcode()).isEqualTo(paymentNotification.receiver());
        assertThat(merchantNotificationCaptor.getValue().keyword()).isEqualTo("TXT");
        assertThat(merchantNotificationCaptor.getValue().message()).isEqualTo(paymentNotification.text());
        assertThat(merchantNotificationCaptor.getValue().operator()).isEqualTo(paymentNotification.operator());
        assertThat(merchantNotificationCaptor.getValue().sender()).isEqualTo(paymentNotification.sender());
        assertThat(merchantNotificationCaptor.getValue().transaction_id()).isNotNull();
        verify(replySender).send(replyCaptor.capture());
        assertThat(replyCaptor.getValue().get("message")).isEqualTo("test passed");
        assertThat(replyCaptor.getValue().get("mo_message_id")).isEqualTo(paymentNotification.message_id());
        assertThat(replyCaptor.getValue().get("receiver")).isEqualTo(paymentNotification.sender());
        assertThat(replyCaptor.getValue().get("operator")).isEqualTo(paymentNotification.operator());
    }

    @Test
    void sendsFORNotificationToMerchant() {
        var paymentNotification = createPaymentNotification("FOR+SUR");
        when(merchantNotificationSender.send(any())).thenReturn(new PaymentNotificationHandler.MerchantReply("test passed"));

        notificationHandler.handle(paymentNotification);

        verify(merchantNotificationSender).send(merchantNotificationCaptor.capture());
        assertThat(merchantNotificationCaptor.getValue().shortcode()).isEqualTo(paymentNotification.receiver());
        assertThat(merchantNotificationCaptor.getValue().keyword()).isEqualTo("FOR");
        assertThat(merchantNotificationCaptor.getValue().message()).isEqualTo(paymentNotification.text());
        assertThat(merchantNotificationCaptor.getValue().operator()).isEqualTo(paymentNotification.operator());
        assertThat(merchantNotificationCaptor.getValue().sender()).isEqualTo(paymentNotification.sender());
        assertThat(merchantNotificationCaptor.getValue().transaction_id()).isNotNull();
        verify(replySender).send(replyCaptor.capture());
        assertThat(replyCaptor.getValue().get("message")).isEqualTo("test passed");
        assertThat(replyCaptor.getValue().get("mo_message_id")).isEqualTo(paymentNotification.message_id());
        assertThat(replyCaptor.getValue().get("receiver")).isEqualTo(paymentNotification.sender());
        assertThat(replyCaptor.getValue().get("operator")).isEqualTo(paymentNotification.operator());
    }

    @Test
    void handlesExceptionThrownByPaymentNotificationReplySender() {
        var paymentNotification = createPaymentNotification("FOR SUR");
        when(merchantNotificationSender.send(any())).thenReturn(new PaymentNotificationHandler.MerchantReply("test passed"));
        doThrow(RuntimeException.class).when(replySender).send(any());

        assertThatNoException().isThrownBy(() -> notificationHandler.handle(paymentNotification));
        verify(replySender).send(anyMap());
    }

    private static PaymentNotification createPaymentNotification(String text) {
        return new PaymentNotification("test-id", "+37255555555", text, "13011", "Etisalat", LocalDateTime.now());
    }
}