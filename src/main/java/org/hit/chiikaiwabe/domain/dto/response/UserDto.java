package org.hit.chiikaiwabe.domain.dto.response;

import org.hit.chiikaiwabe.domain.dto.common.DateAuditingDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDto extends DateAuditingDto {

  private String id;

  private String username;

  private String firstName;

  private String lastName;

  private String email;

  private String phone;

  private String avatar;

  private String majorName;

  private String university;

  private String gender;

  private LocalDate dateOfBirth;

  private String location;

  private Double trustScore;

  private String userstatus;

  private Boolean buddyActive;

  private String statusTag;

  private Boolean deleteFlag;

  private String roleName;

}
