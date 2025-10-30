package org.example.do_an_v1.service.support;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class GcsService {

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    private final Storage storage;
    private static final String GCS_BASE_URL = "https://storage.googleapis.com/";

    public GcsService(Storage storage) {
        this.storage = storage;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Empty file!");
        }

        // Đặt tên file mới
        String extension = ".jpg";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        String newFileName = UUID.randomUUID() + extension;

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, newFileName)
                .setContentType(file.getContentType())
                .setCacheControl("public, max-age=86400")
                .build();

        storage.create(blobInfo, file.getBytes());

        return GCS_BASE_URL + bucketName + "/" + newFileName;
    }
}
