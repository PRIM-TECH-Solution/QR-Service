package com.paymentback.qrgeneration.Service;


import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.File;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendEmailWithQRCode(String to, String subject, String text, List<File> qrCodeFiles, File pdfFile) throws MessagingException, jakarta.mail.MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true); // Enable HTML content

            // Add QR code images as inline attachments
            for (File qrCodeFile : qrCodeFiles) {
                FileSystemResource qrCodeResource = new FileSystemResource(qrCodeFile);
                helper.addInline(qrCodeFile.getName(), qrCodeResource);
            }

            // Add PDF as attachment
            FileSystemResource pdfResource = new FileSystemResource(pdfFile);
            helper.addAttachment(pdfFile.getName(), pdfResource);

            emailSender.send(message);

            // Cleanup temporary files
            for (File file : qrCodeFiles) {
                file.delete();
            }
            pdfFile.delete();
        } finally {
            // Ensure files are deleted even if an exception occurs
            deleteFilesQuietly(qrCodeFiles);
            deleteFileQuietly(pdfFile);
        }
    }

    private void deleteFilesQuietly(List<File> files) {
        for (File file : files) {
            deleteFileQuietly(file);
        }
    }

    private void deleteFileQuietly(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
