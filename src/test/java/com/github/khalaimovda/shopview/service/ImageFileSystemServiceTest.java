package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.config.ImageServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.khalaimovda.shopview.utils.ImageUtils.createRandomBytes;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImageFileSystemServiceTest {
    @TempDir
    private Path tempDir;

    private ImageServiceProperties properties;
    private ImageService imageService;

    @BeforeEach
    void setup() throws IOException {
        properties = new ImageServiceProperties();
        properties.setUploadDir(tempDir.toString());
        properties.setBaseUrl("/images/");
        imageService = new ImageFileSystemService(properties);
    }

//    @Test
//    void testSaveImage() throws IOException {
//        byte[] imageBytes = createRandomBytes();
//        MultipartFile imageFile = new MockMultipartFile(
//            "image", "test.jpg", "image/jpeg", imageBytes
//        );
//
//        String storedFilename = imageService.saveImage(imageFile);
//
//        Path savedFilePath = Paths.get(properties.getUploadDir()).resolve(storedFilename);
//        assertTrue(Files.exists(savedFilePath));
//
//        byte[] savedContent = Files.readAllBytes(savedFilePath);
//        assertArrayEquals(imageBytes, savedContent);
//    }

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

        imageService.deleteImage("test_image.jpg");
        assertFalse(Files.exists(imagePath));
    }
}
