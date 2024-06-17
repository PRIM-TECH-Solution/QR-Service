package com.paymentback.qrgeneration.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.io.source.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeService {

    public BufferedImage generateQRCodeImage(String text) throws WriterException {
        int width = 300;
        int height = 300;

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage qrCodeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                qrCodeImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        // Add logo text to the center
        Graphics2D g = qrCodeImage.createGraphics();
        try {
            // Set the font and its properties
            g.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics fontMetrics = g.getFontMetrics();
            int logoWidth = fontMetrics.stringWidth("EasyTicket.LK");
            int logoHeight = fontMetrics.getHeight();

            // Calculate the coordinates for the text to be centered
            int centerX = (qrCodeImage.getWidth() - logoWidth) / 2;
            int centerY = qrCodeImage.getHeight() / 2 + logoHeight / 4;

            // Draw the background rectangle
            g.setColor(Color.BLACK);
            g.fillRect(centerX - 5, centerY - logoHeight + 5, logoWidth + 10, logoHeight);

            // Draw the text
            g.setColor(Color.WHITE);
            g.drawString("EasyTicket.LK", centerX, centerY);
        } finally {
            g.dispose();
        }

        return qrCodeImage;
    }

    public void saveQRCodeImageToFile(BufferedImage image, File file) throws IOException {
        ImageIO.write(image, "png", file);
    }

    public String encodeBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
