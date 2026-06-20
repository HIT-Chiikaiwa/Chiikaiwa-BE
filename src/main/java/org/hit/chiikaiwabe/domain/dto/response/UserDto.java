package org.hit.chiikaiwabe.domain.dto.response;

import jakarta.persistence.Column;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.Nationalized;
import org.hit.chiikaiwabe.domain.dto.common.DateAuditingDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hit.chiikaiwabe.domain.entity.Role;

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

  private String major_name;

  private String university;

  private String gender;

  private int age;

  private String location;

  private Double trustScore;

  private String status;

  private String roleName;

}

