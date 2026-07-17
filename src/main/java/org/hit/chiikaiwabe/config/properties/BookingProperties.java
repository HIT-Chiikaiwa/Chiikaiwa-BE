package org.hit.chiikaiwabe.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "booking")
public class BookingProperties {

    private int maxActiveBookings;
    private int minAdvanceMinutes;
    private int pendingExpireMinutes;
    private int confirmedExpireHours;
    private int defaultReminderMinutes;

}
