package com.sistema.sistema.application.service;

import com.sistema.sistema.infrastructure.exception.BusinessException;
import com.sistema.sistema.infrastructure.storage.ProductImageProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalFileStorageServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void removesStagedFilesWhenAnyImageIsInvalid() throws Exception {
        ProductImageProcessor processor = new ProductImageProcessor();
        ReflectionTestUtils.setField(processor, "maxWidth", 1600);
        ReflectionTestUtils.setField(processor, "webpQuality", 0.84f);
        ReflectionTestUtils.setField(processor, "minWebpQuality", 0.70f);
        ReflectionTestUtils.setField(processor, "webpQualityStep", 0.05f);
        ReflectionTestUtils.setField(processor, "maxOutputBytes", 256_000);
        ReflectionTestUtils.setField(processor, "maxPixels", 24_000_000L);

        LocalFileStorageService service = new LocalFileStorageService(processor);
        ReflectionTestUtils.setField(service, "basePath", temporaryDirectory.toString());

        assertThrows(BusinessException.class, () -> service.upload("products/1", new MockMultipartFile[]{
                validImage(),
                new MockMultipartFile("images", "forzado.png", "image/png", "PK\u0003\u0004contenido-zip".getBytes())
        }));

        assertEmpty(temporaryDirectory.resolve("uploads").resolve("products").resolve("1"));
        assertEmpty(temporaryDirectory.resolve("uploads").resolve(".tmp"));
    }

    private MockMultipartFile validImage() throws Exception {
        BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.BLUE);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        } finally {
            graphics.dispose();
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        return new MockMultipartFile("images", "valida.jpg", "image/jpeg", output.toByteArray());
    }

    private void assertEmpty(Path directory) throws Exception {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> files = Files.list(directory)) {
            assertEquals(0, files.count());
        }
    }
}
