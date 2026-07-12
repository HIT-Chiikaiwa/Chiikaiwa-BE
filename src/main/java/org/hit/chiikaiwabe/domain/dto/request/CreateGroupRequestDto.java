package org.hit.chiikaiwabe.domain.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateGroupRequestDto {
    @NotBlank private String groupName;
    private String groupAvatar;
    @NotEmpty private List<String> memberIds;
}
