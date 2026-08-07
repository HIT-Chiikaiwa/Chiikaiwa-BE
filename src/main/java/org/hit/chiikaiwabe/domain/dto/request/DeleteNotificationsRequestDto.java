package org.hit.chiikaiwabe.domain.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hit.chiikaiwabe.constant.ErrorMessage;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DeleteNotificationsRequestDto {

    @NotEmpty(message = ErrorMessage.Notification.VAL_IDS_REQUIRED)
    private List<String> notificationIds;
}
