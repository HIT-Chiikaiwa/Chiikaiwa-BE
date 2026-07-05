package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.response.MessageResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    MessageResponseDto uploadFile(String userId, String conversationId, MultipartFile file);
}
