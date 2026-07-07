package org.hit.chiikaiwabe.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.File;

@Service
public class FileServiceImpl implements FileService {

    private final Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx"
    );

    public FileServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
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

        try {
            File tempFile = File.createTempFile("upload_", "_" + originalFilename);
            file.transferTo(tempFile);
            try {
                Map uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.asMap("resource_type", "auto"));
                return uploadResult.get("secure_url").toString();
            } finally {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(ErrorMessage.File.ERR_FILE_UPLOAD_FAILED, e);
        }
    }
}
