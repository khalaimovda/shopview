package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.config.ImageServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.khalaimovda.shopview.showcase.utils.ImageUtils.createRandomBytes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageFileSystemServiceTest {
    @TempDir
    private Path tempDir;

    @Mock
    FilePart imageFile;

    @Captor
    ArgumentCaptor<Path> pathCaptor;

    private ImageServiceProperties properties;
    private ImageService imageService;

    @BeforeEach
    void setup() throws IOException {
        properties = new ImageServiceProperties();
        properties.setUploadDir(tempDir.toString());
        properties.setBaseUrl("/images/");
        imageService = new ImageFileSystemService(properties);
    }

    @Test
    void testSaveImage() throws IOException {
        String imageName = "test_image.jpg";
        when(imageFile.filename()).thenReturn(imageName);
        when(imageFile.transferTo(any(Path.class))).thenReturn(Mono.empty());

        Mono<String> storedFilename = imageService.saveImage(imageFile);

        StepVerifier
            .create(storedFilename)
            .expectNextMatches(filename -> filename.endsWith(imageName))
            .verifyComplete();
        verify(imageFile, times(1)).transferTo(pathCaptor.capture());
        assertTrue(pathCaptor.getValue().getFileName().toString().endsWith(imageName));

    }

    @Test
    void testGetImagePath() {
        String filename = "test_image.jpg";
        Path correctImagePath = Paths.get(properties.getUploadDir()).resolve(filename);
        Path imagePath = imageService.getImagePath(filename);
        assertEquals(correctImagePath, imagePath);
    }

    @Test
    void testGetImageSrcPath() {
        assertEquals(properties.getBaseUrl() + "test_image.jpg", imageService.getImageSrcPath("test_image.jpg"));
    }

    @Test
    void testDeleteImage() throws IOException {
        byte[] imageBytes = createRandomBytes();
        Path imagePath = Paths.get(properties.getUploadDir()).resolve("test_image.jpg");
        Files.write(imagePath, imageBytes);
        assertTrue(Files.exists(imagePath));

        Mono<Void> monoResult = imageService.deleteImage("test_image.jpg");
        StepVerifier
            .create(monoResult)
            .verifyComplete();
        assertFalse(Files.exists(imagePath));
    }
}
