package org.hit.chiikaiwabe.domain.mapper;

import org.hit.chiikaiwabe.domain.dto.request.UserCreateDto;
import org.hit.chiikaiwabe.domain.dto.response.UserDto;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.UserTitle;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  User toUser(UserCreateDto userCreateDTO);

  @Mappings({
          @Mapping(target = "roleName", expression = "java(user.getRole().name())"),
  })
  UserDto toUserDto(User user);

  List<UserDto> toUserDtos(List<User> user);

  @AfterMapping
  default void setTitleIcon(User user, @MappingTarget UserDto dto) {
    if (user.getExpPoints() != null) {
      UserTitle title = UserTitle.fromExp(user.getExpPoints());
      dto.setTitleIcon(title.getIcon());
    }
  }

}
