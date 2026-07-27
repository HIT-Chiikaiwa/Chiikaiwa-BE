package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.util.UploadFileUtil;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {

    private final UploadFileUtil uploadFileUtil;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "heif",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx"
    );

    public FileServiceImpl(UploadFileUtil uploadFileUtil) {
        this.uploadFileUtil = uploadFileUtil;
    }

    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidException(ErrorMessage.File.ERR_FILE_EMPTY);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidException(ErrorMessage.File.ERR_FILE_SIZE_EXCEED);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new InvalidException(ErrorMessage.File.ERR_FILE_TYPE_NOT_ALLOWED);
            }
        } else {
            throw new InvalidException(ErrorMessage.File.ERR_FILE_NAME_INVALID);
        }

        return uploadFileUtil.uploadFile(file);
    }
}
