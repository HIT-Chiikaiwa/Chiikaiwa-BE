package org.hit.chiikaiwabe.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("radar")
public class RadarProperties {
    private Double defaultRadiusKm = 5.0;
    private Double maxRadiusKm = 20.0;
    private Integer ttlMinutes = 30;
}
