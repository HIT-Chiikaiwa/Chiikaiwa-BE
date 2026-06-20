package org.hit.chiikaiwabe.domain.entity;

import org.hit.chiikaiwabe.domain.entity.common.DateAuditing;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Nationalized;

import jakarta.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "users")
public class User extends DateAuditing {

  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(insertable = false, updatable = false, nullable = false, columnDefinition = "CHAR(36)")
  private String id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  @JsonIgnore
  private String password;

  @Nationalized
  @Column(nullable = false)
  private String firstName;

  @Nationalized
  @Column(nullable = false)
  private String lastName;

  @Column(unique = true)
  private String email;

  @Column(unique = true, length = 10)
  private String phone;

  @Column(nullable = true)
  private String avatar;

  @Nationalized
  @Column(nullable = true, name = "major_name")
  private String majorName;

  @Nationalized
  @Column(nullable = true)
  private String university;

  @Column(nullable = false, length = 10)
  private String gender;

  @Column(nullable = false)
  private int age;

  @Nationalized
  @Column(nullable = false)
  private String location;

  @Column(nullable = false, name = "trust_score")
  private Double trustScore;

  @Column(nullable = false)
  private String status;

  //Link to table Role
  @ManyToOne
  @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "FK_USER_ROLE"))
  private Role role;



}
