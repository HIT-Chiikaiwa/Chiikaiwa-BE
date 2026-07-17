package org.hit.chiikaiwabe.domain.dto.response;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingWeekResponseDto {

    private LocalDate weekStart;
    private LocalDate weekEnd;
    private Map<DayOfWeek, List<BookingResponseDto>> daySchedules;

}
