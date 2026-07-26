package org.hit.chiikaiwabe.domain.entity;

import org.hit.chiikaiwabe.domain.enums.Role;
import org.hit.chiikaiwabe.domain.enums.UserStatus;
import org.hit.chiikaiwabe.domain.entity.common.DateAuditing;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import jakarta.persistence.*;
import java.time.LocalDate;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "users")
public class User extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    @Column(name = "date_of_birth",nullable = true)
    private LocalDate dateOfBirth;

    @Nationalized
    @Column(nullable = true)
    private String location;

    @Column(nullable = false, name = "trust_score")
    private Double trustScore;

    @Column(name = "total_rating_count")
    @Builder.Default
    private Integer totalRatingCount = 0;

    @Column(name = "exp_points", nullable = false)
    @Builder.Default
    private Long expPoints = 0L;

    @Nationalized
    @Column(name = "title", length = 30)
    @Builder.Default
    private String title = "Tân Binh";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus userstatus;

    @Column(name = "buddy_active")
    @Builder.Default
    private Boolean buddyActive = Boolean.FALSE;

    @Nationalized
    @Column(name = "status_tag")
    private String statusTag;

    @Column(name = "delete_flag")
    @Builder.Default
    private Boolean deleteFlag = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @PostLoad
    private void onPostLoad() {
        if (totalRatingCount == null) totalRatingCount = 0;
        if (buddyActive == null) buddyActive = false;
        if (deleteFlag == null) deleteFlag = false;
        if (expPoints == null) expPoints = 0L;
        if (title == null) title = "Tân Binh";
    }

}
