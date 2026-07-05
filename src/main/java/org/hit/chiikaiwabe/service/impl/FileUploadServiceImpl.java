package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.domain.dto.response.MessageResponseDto;
import org.hit.chiikaiwabe.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Override
    public MessageResponseDto uploadFile(String userId, String conversationId, MultipartFile file) {
        return null; // TODO
    }
}
