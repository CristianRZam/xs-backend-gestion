package com.sistema.sistema.infrastructure.storage;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductImageProcessorTest {

    @Test
    void convertsJpegToWebpAndResizesItsLargestSide() throws Exception {
        ProductImageProcessor processor = new ProductImageProcessor();
        ReflectionTestUtils.setField(processor, "maxWidth", 1600);
        ReflectionTestUtils.setField(processor, "webpQuality", 0.84f);
        ReflectionTestUtils.setField(processor, "minWebpQuality", 0.70f);
        ReflectionTestUtils.setField(processor, "webpQualityStep", 0.05f);
        ReflectionTestUtils.setField(processor, "maxOutputBytes", 256_000);
        ReflectionTestUtils.setField(processor, "maxPixels", 24_000_000L);

        BufferedImage source = new BufferedImage(2000, 1000, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = source.createGraphics();
        try {
            graphics.setColor(Color.ORANGE);
            graphics.fillRect(0, 0, source.getWidth(), source.getHeight());
        } finally {
            graphics.dispose();
        }

        ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
        ImageIO.write(source, "jpg", jpeg);
        MockMultipartFile file = new MockMultipartFile(
                "images", "producto.jpg", "image/jpeg", jpeg.toByteArray());

        ProductImageProcessor.ProcessedImage result = processor.process(file);

        BufferedImage optimized = ImageIO.read(new ByteArrayInputStream(result.content()));
        assertNotNull(optimized);
        assertEquals("producto.jpg", result.originalName());
        assertEquals(1600, optimized.getWidth());
        assertEquals(800, optimized.getHeight());
        assertTrue(result.content().length <= 256_000);
    }
}
