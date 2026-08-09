package org.hit.chiikaiwabe.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserCreateDto {


  @NotBlank(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
  @Email(message = ErrorMessage.INVALID_SOME_THING_FIELD)
  private String email;

  @NotNull(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
  @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = ErrorMessage.Auth.INVALID_FORMAT_PASSWORD_COMPLEX)
  private String password;

  @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
  private String confirmPassword;

  @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
  private String firstName;

  @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
  private String lastName;

  @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
  private String gender;

  @NotNull(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
  @Past(message = ErrorMessage.INVALID_DATE_PAST)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate dateOfBirth;

}
