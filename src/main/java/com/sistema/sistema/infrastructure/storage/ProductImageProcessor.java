package com.sistema.sistema.infrastructure.storage;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

@Component
@Slf4j
public class ProductImageProcessor {

    private static final Set<String> SUPPORTED_FORMATS = Set.of("jpeg", "jpg", "png", "webp");

    @Value("${app.product-images.max-width}")
    private int maxWidth;

    @Value("${app.product-images.webp-quality}")
    private float webpQuality;

    @Value("${app.product-images.min-webp-quality}")
    private float minWebpQuality;

    @Value("${app.product-images.webp-quality-step}")
    private float webpQualityStep;

    @Value("${app.product-images.max-output-bytes}")
    private int maxOutputBytes;

    @Value("${app.product-images.max-pixels}")
    private long maxPixels;

    public ProcessedImage process(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La imagen del producto está vacía.");
        }

        byte[] source = file.getBytes();
        BufferedImage image = readAndValidate(source);
        BufferedImage orientedImage = applyExifOrientation(image, source);
        BufferedImage resizedImage = resize(orientedImage);
        OptimizedWebp optimized = optimizeWebp(resizedImage);
        log.debug("Imagen de producto optimizada de {}x{} a {}x{} y de {} a {} bytes",
                image.getWidth(), image.getHeight(), optimized.image().getWidth(), optimized.image().getHeight(),
                source.length, optimized.content().length);

        return new ProcessedImage(optimized.content(), originalName(file));
    }

    private BufferedImage readAndValidate(byte[] source) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(source))) {
            Iterator<javax.imageio.ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw invalidImage();
            }

            javax.imageio.ImageReader reader = readers.next();
            try {
                reader.setInput(input, false, false);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!SUPPORTED_FORMATS.contains(format)) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST,
                            "Formato no permitido. Use JPG, PNG o WebP.");
                }

                if (reader.getNumImages(true) != 1) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST,
                            "No se permiten imágenes animadas para productos.");
                }

                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels > maxPixels) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST,
                            "La imagen supera la resolución máxima permitida.");
                }

                BufferedImage image = reader.read(0);
                if (image == null) {
                    throw invalidImage();
                }
                return image;
            } finally {
                reader.dispose();
            }
        }
    }

    private BufferedImage applyExifOrientation(BufferedImage image, byte[] source) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(source));
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            Integer orientation = directory == null ? null : directory.getInteger(ExifIFD0Directory.TAG_ORIENTATION);
            return orientation == null ? image : transformOrientation(image, orientation);
        } catch (ImageProcessingException | IOException ignored) {
            return image;
        }
    }

    private BufferedImage transformOrientation(BufferedImage image, int orientation) {
        int width = image.getWidth();
        int height = image.getHeight();
        AffineTransform transform = new AffineTransform();
        int targetWidth = width;
        int targetHeight = height;

        switch (orientation) {
            case 2 -> {
                transform.translate(width, 0);
                transform.scale(-1, 1);
            }
            case 3 -> {
                transform.translate(width, height);
                transform.rotate(Math.PI);
            }
            case 4 -> {
                transform.translate(0, height);
                transform.scale(1, -1);
            }
            case 5 -> {
                targetWidth = height;
                targetHeight = width;
                transform.translate(height, 0);
                transform.rotate(Math.PI / 2);
                transform.scale(-1, 1);
            }
            case 6 -> {
                targetWidth = height;
                targetHeight = width;
                transform.translate(height, 0);
                transform.rotate(Math.PI / 2);
            }
            case 7 -> {
                targetWidth = height;
                targetHeight = width;
                transform.translate(0, width);
                transform.rotate(-Math.PI / 2);
                transform.scale(-1, 1);
            }
            case 8 -> {
                targetWidth = height;
                targetHeight = width;
                transform.translate(0, width);
                transform.rotate(-Math.PI / 2);
            }
            default -> {
                return image;
            }
        }

        BufferedImage result = createImage(targetWidth, targetHeight, image);
        Graphics2D graphics = result.createGraphics();
        try {
            graphics.drawImage(image, transform, null);
        } finally {
            graphics.dispose();
        }
        return result;
    }

    private BufferedImage resize(BufferedImage image, int targetWidth, int targetHeight) {
        BufferedImage result = createImage(targetWidth, targetHeight, image);
        Graphics2D graphics = result.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return result;
    }

    private BufferedImage resize(BufferedImage image) {
        int largestSide = Math.max(image.getWidth(), image.getHeight());
        if (largestSide <= maxWidth) {
            return image;
        }

        double scale = (double) maxWidth / largestSide;
        int targetWidth = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int targetHeight = Math.max(1, (int) Math.round(image.getHeight() * scale));
        return resize(image, targetWidth, targetHeight);
    }

    private OptimizedWebp optimizeWebp(BufferedImage image) throws IOException {
        float quality = normalizedQuality(webpQuality);
        float minimumQuality = Math.min(quality, normalizedQuality(minWebpQuality));
        float qualityStep = Math.max(0.01f, normalizedQuality(webpQualityStep));
        int targetSize = Math.max(1, maxOutputBytes);
        BufferedImage optimizedImage = image;
        byte[] result = encodeWebp(optimizedImage, quality);

        while (result.length > targetSize && quality > minimumQuality) {
            quality = Math.max(minimumQuality, quality - qualityStep);
            result = encodeWebp(optimizedImage, quality);
        }

        while (result.length > targetSize
                && (optimizedImage.getWidth() > 1 || optimizedImage.getHeight() > 1)) {
            int targetWidth = Math.max(1, (int) Math.floor(optimizedImage.getWidth() * 0.9));
            int targetHeight = Math.max(1, (int) Math.floor(optimizedImage.getHeight() * 0.9));
            optimizedImage = resize(optimizedImage, targetWidth, targetHeight);
            result = encodeWebp(optimizedImage, minimumQuality);
        }

        return new OptimizedWebp(optimizedImage, result);
    }

    private byte[] encodeWebp(BufferedImage image, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        if (!writers.hasNext()) {
            throw new IOException("El codificador WebP no está disponible.");
        }

        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(imageOutput);
            ImageWriteParam parameters = writer.getDefaultWriteParam();
            if (parameters.canWriteCompressed()) {
                parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                String[] compressionTypes = parameters.getCompressionTypes();
                if (compressionTypes != null && compressionTypes.length > 0) {
                    parameters.setCompressionType(compressionTypes[0]);
                }
                parameters.setCompressionQuality(quality);
            }
            writer.write(null, new IIOImage(image, null, null), parameters);
            imageOutput.flush();
            return output.toByteArray();
        } finally {
            writer.dispose();
        }
    }

    private float normalizedQuality(float quality) {
        return Math.max(0, Math.min(1, quality));
    }

    private BufferedImage createImage(int width, int height, BufferedImage source) {
        return new BufferedImage(width, height,
                source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
    }

    private String originalName(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        return originalName == null || originalName.isBlank() ? "imagen" : originalName;
    }

    private BusinessException invalidImage() {
        return new BusinessException(HttpStatus.BAD_REQUEST,
                "El archivo no contiene una imagen válida.");
    }

    public record ProcessedImage(byte[] content, String originalName) {
    }

    private record OptimizedWebp(BufferedImage image, byte[] content) {
    }
}
