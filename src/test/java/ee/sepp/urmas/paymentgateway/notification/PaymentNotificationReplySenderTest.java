package ee.sepp.urmas.paymentgateway.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ee.sepp.urmas.paymentgateway.configuration.PaymentGatewayProperties;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class PaymentNotificationReplySenderTest {

    private PaymentNotificationReplySender replySender;

    @Mock
    private PaymentGatewayProperties properties;

    private MockWebServer server;

    @BeforeEach
    void setUp() {
        when(properties.getTimeout()).thenReturn(Duration.ofSeconds(1));
        server = new MockWebServer();
        var webClient = WebClient.builder().baseUrl(String.valueOf(server.url("/sms/send"))).build();
        replySender = new PaymentNotificationReplySender(properties, webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void sendsReply() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()).setBody("OK"));
        var parameters = createParameters();

        replySender.send(parameters);

        assertThat(server.getRequestCount()).isEqualTo(1);
        var requestUrl = server.takeRequest().getRequestUrl();
        assertThat(requestUrl).isNotNull();
        assertThat(requestUrl.queryParameter("message")).isEqualTo("test passed");
        assertThat(requestUrl.queryParameter("mo_message_id")).isEqualTo("test-id");
        assertThat(requestUrl.queryParameter("receiver")).isEqualTo("37255555555");
        assertThat(requestUrl.queryParameter("operator")).isEqualTo("Etisalat");
    }

    @Test
    void throwsExceptionWhenResponseNotOk() {
        server.enqueue(new MockResponse().setResponseCode(HttpStatus.NO_CONTENT.value()));

        assertThatThrownBy(() -> replySender.send(createParameters())).isInstanceOf(RuntimeException.class);
    }

    private Map<String, ?> createParameters() {
        return Map.of("message", "test passed",
                      "mo_message_id", "test-id",
                      "receiver", "37255555555",
                      "operator", "Etisalat");
    }
}