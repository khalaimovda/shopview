package com.github.khalaimovda.shopview.service;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.nio.file.Path;

public interface ImageService {
    Mono<String> saveImage(FilePart file);
    Mono<Void> deleteImage(String fileName);
    Path getImagePath(String fileName);
    String getImageSrcPath(String fileName);
}
