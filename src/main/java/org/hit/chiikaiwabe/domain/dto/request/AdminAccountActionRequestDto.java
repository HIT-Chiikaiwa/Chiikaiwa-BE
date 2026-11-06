package org.hit.chiikaiwabe.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hit.chiikaiwabe.constant.ErrorMessage;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminAccountActionRequestDto {
    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @Size(max = 1000, message = ErrorMessage.Admin.INVALID_REASON_LENGTH)
    private String reason;

    @Size(max = 36, message = ErrorMessage.Admin.INVALID_REPORT_ID)
    private String reportId;
}
