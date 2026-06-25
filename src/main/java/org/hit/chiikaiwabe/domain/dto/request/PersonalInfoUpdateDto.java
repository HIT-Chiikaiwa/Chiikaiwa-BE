package org.hit.chiikaiwabe.domain.dto.request;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PersonalInfoUpdateDto {

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String firstName;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String lastName;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String gender;

    @NotNull(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private String phone;

    @Email(message = ErrorMessage.INVALID_FORMAT_EMAIL)
    private String email;

}

