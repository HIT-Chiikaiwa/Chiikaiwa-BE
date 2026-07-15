package org.hit.chiikaiwabe.domain.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import org.hit.chiikaiwabe.constant.ErrorMessage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequestDto {
    private String subject;

    @NotNull(message = ErrorMessage.Booking.VAL_SCHEDULED_AT_REQUIRED)
    @Future(message = ErrorMessage.Booking.VAL_SCHEDULED_AT_FUTURE)
    private LocalDateTime scheduledAt;

    private Integer durationMinutes;

    @NotBlank(message = ErrorMessage.Booking.VAL_LOCATION_NAME_REQUIRED)
    private String locationName;

    @NotBlank(message = ErrorMessage.Booking.VAL_LOCATION_ADDRESS_REQUIRED)
    private String locationAddress;

    @NotBlank(message = ErrorMessage.Booking.VAL_LOCATION_DISTRICT_REQUIRED)
    private String locationDistrict;

    @NotBlank(message = ErrorMessage.Booking.VAL_LOCATION_CITY_REQUIRED)
    private String locationCity;

    private String note;

    private Boolean isRecurring = false;

    private Integer reminderMinutesBefore;
}
