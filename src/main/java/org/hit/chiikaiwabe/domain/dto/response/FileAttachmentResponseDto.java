package org.hit.chiikaiwabe.domain.dto.response;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FileAttachmentResponseDto {
    private String id;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
}
