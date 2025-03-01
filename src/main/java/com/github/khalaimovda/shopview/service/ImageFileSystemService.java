package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.config.ImageServiceProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@EnableConfigurationProperties(ImageServiceProperties.class)
@RequiredArgsConstructor
public class ImageFileSystemService implements ImageService {

    private final ImageServiceProperties properties;

    @PostConstruct
    public void init() throws IOException {
        Path imageStoragePath = Paths.get(properties.getUploadDir());
        if (!Files.exists(imageStoragePath)) {
            Files.createDirectories(imageStoragePath);
        }
    }

    @Override
    public String saveImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Path filePath = getImagePath(fileName);
        try {
            Files.copy(file.getInputStream(), filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fileName;
    }

    @Override
    public Path getImagePath(String fileName) {
        return Paths.get(properties.getUploadDir()).resolve(fileName).normalize();
    }

    @Override
    public String getImageSrcPath(String fileName) {
        return properties.getBaseUrl() + fileName;
    }

    @Override
    public void deleteImage(String fileName) {
        Path filePath = getImagePath(fileName);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
