package org.hit.chiikaiwabe.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("radar")
@Component
@Getter
@Setter
public class RadarProperties {
    private int defaultRadiusKm;
    private int maxRadiusKm;
    private int timeToLiveMinutes;
}
