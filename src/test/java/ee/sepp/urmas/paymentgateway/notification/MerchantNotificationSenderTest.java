package ee.sepp.urmas.paymentgateway.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ee.sepp.urmas.paymentgateway.configuration.PaymentGatewayProperties;
import ee.sepp.urmas.paymentgateway.notification.PaymentNotificationHandler.MerchantNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class MerchantNotificationSenderTest {

    private MerchantNotificationSender sender;

    @Mock
    private PaymentGatewayProperties properties;
    @Mock
    private ExchangeFunction exchangeFunction;

    @Captor
    private ArgumentCaptor<ClientRequest> requestCaptor;

    @BeforeEach
    void setUp() {
        var webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        sender = new MerchantNotificationSender(properties, webClient);
    }

    @Test
    void returnsMerchantResponseForKeywordTXT() {
        when(properties.getMerchantEndpointTxt()).thenReturn("/txt");
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(clientResponse());

        var response = sender.send(createNotification("TXT"));

        verify(exchangeFunction).exchange(requestCaptor.capture());
        verify(properties, never()).getMerchantEndpointFor();
        assertThat(requestCaptor.getValue().url().getPath()).isEqualTo("/txt");
        assertThat(response.reply_message()).isEqualTo("test passed");
    }

    @Test
    void returnsMerchantResponseForKeywordFOR() {
        when(properties.getMerchantEndpointFor()).thenReturn("/for");
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(clientResponse());

        var response = sender.send(createNotification("FOR"));

        verify(exchangeFunction).exchange(requestCaptor.capture());
        verify(properties, never()).getMerchantEndpointTxt();
        assertThat(requestCaptor.getValue().url().getPath()).isEqualTo("/for");
        assertThat(response.reply_message()).isEqualTo("test passed");
    }

    @Test
    void returnsErrorMessageWhenWrongKeyword() {
        var response = sender.send(createNotification("XXX"));

        assertThat(response.reply_message()).isEqualTo("Something went wrong. Please contact us at cs.boku.com to receive your service");
    }

    @Test
    void returnsErrorMessageWhenMerchantResponseNotOk() {
        when(properties.getMerchantEndpointFor()).thenReturn("/for");
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(notOkResponse());

        var response = sender.send(createNotification("FOR"));

        assertThat(response.reply_message()).isEqualTo("Something went wrong. Please contact us at cs.boku.com to receive your service");
    }

    private Mono<ClientResponse> clientResponse() {
        return Mono.just(ClientResponse.create(HttpStatus.OK)
                                 .header("content-type", MediaType.APPLICATION_JSON_VALUE)
                                 .body("{ \"reply_message\" : \"test passed\"}")
                                 .build());
    }

    private Mono<ClientResponse> notOkResponse() {
        return Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build());
    }

    private MerchantNotification createNotification(String keyword) {
        return new MerchantNotification("13011", keyword, keyword + " MSG", "Etisalat", "++37255555555", "1");
    }
}