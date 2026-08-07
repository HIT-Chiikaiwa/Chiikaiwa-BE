package org.hit.chiikaiwabe.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hit.chiikaiwabe.domain.enums.ReportReason;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReportRequestDto {
    private String reportedId;
    private String conversationId;
    private String messageId;
    private ReportReason reason;
    private String description;
}
