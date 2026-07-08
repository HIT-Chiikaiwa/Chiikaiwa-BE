package org.hit.chiikaiwabe.domain.mapper;

import org.hit.chiikaiwabe.domain.dto.request.SubjectCreateDto;
import org.hit.chiikaiwabe.domain.dto.response.SubjectDto;
import org.hit.chiikaiwabe.domain.entity.Subject;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface SubjectMapper {

    Subject toSubject(SubjectCreateDto dto);

    SubjectDto toSubjectDto(Subject subject);

    List<SubjectDto> toSubjectDtos(List<Subject> subjects);

}
