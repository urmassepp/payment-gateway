package ee.sepp.urmas.paymentgateway.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentNotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentNotificationHandler notificationHandler;

    @Captor
    private ArgumentCaptor<PaymentNotification> paymentNotificationCaptor;

    @Test
    void onInvalidParametersReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/sms"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$",
                                    containsInAnyOrder("sender - must not be blank",
                                                       "receiver - must not be blank",
                                                       "operator - must not be blank",
                                                       "text - must not be blank",
                                                       "message_id - must not be blank",
                                                       "timestamp - must not be null")));
    }

    @Test
    void onCorrectRequestReturnsOkAndStartsHandling() throws Exception {
        mockMvc.perform(get("/api/v1/sms?message_id=test-id&sender=+37255555555&text=TXT TEST&operator=Etisalat&receiver=13011&timestamp=2023-09-28 10:20:30"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("OK"));

        verify(notificationHandler).handle(paymentNotificationCaptor.capture());
        assertThat(paymentNotificationCaptor.getValue().message_id()).isEqualTo("test-id");
        assertThat(paymentNotificationCaptor.getValue().sender()).isEqualTo("+37255555555");
        assertThat(paymentNotificationCaptor.getValue().text()).isEqualTo("TXT TEST");
        assertThat(paymentNotificationCaptor.getValue().operator()).isEqualTo("Etisalat");
        assertThat(paymentNotificationCaptor.getValue().receiver()).isEqualTo("13011");
        assertThat(paymentNotificationCaptor.getValue().timestamp()).isEqualTo(LocalDateTime.of(2023, 9, 28,10, 20, 30));
    }
}