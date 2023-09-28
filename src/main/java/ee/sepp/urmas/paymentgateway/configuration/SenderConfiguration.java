package ee.sepp.urmas.paymentgateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableRetry
public class SenderConfiguration {

    @Bean
    public WebClient merchantWebClient(PaymentGatewayProperties properties) {
        return WebClient.create(properties.getMerchantBaseUrl());
    }

    @Bean
    public WebClient providerWebClient(PaymentGatewayProperties properties) {
        return WebClient.builder().baseUrl(properties.getProviderReplyUrl())
                .defaultHeaders(header -> header.setBasicAuth(properties.getProviderReplyUsername(),
                                                              properties.getProviderReplyPassword()))
                .build();
    }
}
