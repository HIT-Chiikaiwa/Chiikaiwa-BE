package org.hit.chiikaiwabe.domain.dto.request;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AcademicInfoUpdateDto {

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String university;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String majorName;

}
