package ee.sepp.urmas.paymentgateway.configuration;

import java.time.Duration;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@Configuration
public class PaymentGatewayProperties {

    @Value("${merchant.base.url}")
    private String merchantBaseUrl;

    @Value("${merchant.endpoint.for}")
    private String merchantEndpointFor;

    @Value("${merchant.endpoint.txt}")
    private String merchantEndpointTxt;

    @Value("${provider.reply.url}")
    private String providerReplyUrl;

    @Value("${provider.reply.username}")
    private String providerReplyUsername;

    @Value("${provider.reply.password}")
    private String providerReplyPassword;

    @Value("${timeout.seconds}")
    public void setTimeout(int timeout) {
        this.timeout = Duration.ofSeconds(timeout);
    }

    private Duration timeout;
}
