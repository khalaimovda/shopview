package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.config.ImageServiceProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.UncheckedIOException;
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
    public Mono<String> saveImage(FilePart file) {
        String fileName = System.currentTimeMillis() + "_" + file.filename();
        Path filePath = getImagePath(fileName);
        return file
            .transferTo(filePath)
            .then(Mono.just(fileName));
    }

    @Override
    public Mono<Void> deleteImage(String fileName) {
        Path filePath = getImagePath(fileName);
        return Mono.fromRunnable(() -> {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            })
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    @Override
    public Path getImagePath(String fileName) {
        return Paths.get(properties.getUploadDir()).resolve(fileName).normalize();
    }

    @Override
    public String getImageSrcPath(String fileName) {
        return properties.getBaseUrl() + fileName;
    }
}
