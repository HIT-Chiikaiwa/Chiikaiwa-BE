package org.hit.chiikaiwabe.domain.dto.request;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AddMembersRequestDto {
    @NotEmpty private List<String> memberIds;
}
