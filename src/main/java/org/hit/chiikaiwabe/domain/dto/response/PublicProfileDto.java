package org.hit.chiikaiwabe.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PublicProfileDto {

    private String id;

    private String firstName;

    private String lastName;

    private String avatar;

    private String university;

    private String majorName;

    private String gender;

    private int age;

    private String location;

    private Double trustScore;

    private Boolean buddyActive;

    private String statusTag;

    private List<SubjectDto> subjects;

}
