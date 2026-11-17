package br.com.victordecurtis.livrariadigital.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "bibliotech.rate-limit")
public class RateLimitProperties {
    private long ipLimit = 120;
    private long baseLimit = 30;
    private long premiumLimit = 100;
    private Duration window = Duration.ofMinutes(1);

    public long getIpLimit() { return ipLimit; }
    public void setIpLimit(long ipLimit) { this.ipLimit = ipLimit; }
    public long getBaseLimit() { return baseLimit; }
    public void setBaseLimit(long baseLimit) { this.baseLimit = baseLimit; }
    public long getPremiumLimit() { return premiumLimit; }
    public void setPremiumLimit(long premiumLimit) { this.premiumLimit = premiumLimit; }
    public Duration getWindow() { return window; }
    public void setWindow(Duration window) { this.window = window; }
}
