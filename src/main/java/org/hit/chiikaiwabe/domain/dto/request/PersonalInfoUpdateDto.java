package org.hit.chiikaiwabe.domain.dto.request;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

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
    @Min(value = 17, message = "Age must be at least 17")
    @Max(value = 100, message = "Age must be at most 100")
    private Integer age;

    private String email;

    private String phone;

}
