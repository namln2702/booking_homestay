package org.example.do_an_v1.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;
import com.google.zxing.qrcode.QRCodeReader;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.example.do_an_v1.exception.PaymentQrProcessingException;

@Slf4j
@UtilityClass
public class QrDecoderUtil {

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 5000;

    public static BufferedImage downloadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Image URL is required");
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URL(imageUrl.trim());
            String protocol = url.getProtocol();
            if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
                throw new IllegalArgumentException("Only HTTP/HTTPS image URLs are supported");
            }

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "HomestayPaymentQrValidator/1.0");

            int status = connection.getResponseCode();
            if (status >= 400) {
                throw new PaymentQrProcessingException("Unable to download image. Remote server responded with status " + status);
            }

            try (InputStream inputStream = connection.getInputStream()) {
                BufferedImage image = ImageIO.read(inputStream);
                if (image == null) {
                    throw new PaymentQrProcessingException("Image could not be decoded. Unsupported format or corrupted data");
                }
                return image;
            }
        } catch (IOException ex) {
            throw new PaymentQrProcessingException("Unable to download or read image", ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static List<String> decodeQrPayloads(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            return List.of();
        }

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.QR_CODE));
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        var luminanceSource = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));

        List<String> payloads = new ArrayList<>();
        QRCodeReader qrCodeReader = new QRCodeReader();
        MultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(qrCodeReader);

        try {
            Result[] results = multiReader.decodeMultiple(binaryBitmap, hints);
            for (Result result : results) {
                payloads.add(result.getText());
            }
            return payloads;
        } catch (NotFoundException multiNotFound) {
            log.debug("Multiple QR decode did not find results: {}", multiNotFound.getMessage());
        }

        try {
            Result result = qrCodeReader.decode(binaryBitmap, hints);
            payloads.add(result.getText());
        } catch (NotFoundException singleNotFound) {
            log.debug("Single QR decode did not find a result: {}", singleNotFound.getMessage());
        } catch (Exception ex) {
            throw new PaymentQrProcessingException("Unable to decode QR content from image", ex);
        } finally {
            qrCodeReader.reset();
        }

        return payloads;
    }
}
