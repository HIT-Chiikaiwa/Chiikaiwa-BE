package org.hit.chiikaiwabe.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hit.chiikaiwabe.constant.ErrorMessage;



@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SendOtpRequestDto {

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @Email(message = ErrorMessage.INVALID_SOME_THING_FIELD)
    private String email;

}