package org.hit.chiikaiwabe.domain.dto.request;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResetPasswordRequestDto {
    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @Email(message = ErrorMessage.INVALID_SOME_THING_FIELD)
    private String email;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = ErrorMessage.Auth.INVALID_FORMAT_PASSWORD_COMPLEX)
    private String newPassword;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String confirmPassword;
}