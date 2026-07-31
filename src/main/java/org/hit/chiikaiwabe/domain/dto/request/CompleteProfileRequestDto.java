package org.hit.chiikaiwabe.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hit.chiikaiwabe.constant.ErrorMessage;

import jakarta.validation.constraints.Past;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteProfileRequestDto {
    @NotBlank(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
    private String ticket;
    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String firstName;
    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String lastName;
    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String gender;
    @NotNull(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
    @Past(message = ErrorMessage.INVALID_DATE_PAST)
    private LocalDate dateOfBirth;
}
