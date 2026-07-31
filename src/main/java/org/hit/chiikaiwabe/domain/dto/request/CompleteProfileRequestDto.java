package org.hit.chiikaiwabe.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Past;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteProfileRequestDto {
    @NotBlank(message = "{invalid.general.required}")
    private String ticket;
    @NotBlank(message = "{invalid.general.not-blank}")
    private String firstName;
    @NotBlank(message = "{invalid.general.not-blank}")
    private String lastName;
    @NotBlank(message = "{invalid.general.not-blank}")
    private String gender;
    @NotNull(message = "{invalid.general.required}")
    @Past(message = "{invalid.date-future}")
    private LocalDate dateOfBirth;
}
