package com.paymentback.qrgeneration.Controller;

import com.google.zxing.WriterException;
import com.paymentback.qrgeneration.Entity.Ticket;
import com.paymentback.qrgeneration.Repository.TicketRepository;
import com.paymentback.qrgeneration.Service.EmailService;
import com.paymentback.qrgeneration.Service.PDFGenerationService;
import com.paymentback.qrgeneration.Service.QRCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class QRCodeController {

    private final TicketRepository ticketRepository;
    private final QRCodeService qrCodeService;
    private final PDFGenerationService pdfGenerationService;
    private final EmailService emailService;

    public QRCodeController(TicketRepository ticketRepository, QRCodeService qrCodeService, PDFGenerationService pdfGenerationService, EmailService emailService) {
        this.ticketRepository = ticketRepository;
        this.qrCodeService = qrCodeService;
        this.pdfGenerationService = pdfGenerationService;
        this.emailService = emailService;
    }

    @PostMapping("/generateQRAndSendEmail/{orderId}")
    public ResponseEntity<String> generateQRAndSendEmail(@PathVariable Long orderId) {
        try {
            // Fetch ticket details from the database for the given orderId
            List<Ticket> tickets = ticketRepository.findByOrderId(orderId);

            // Group tickets by email
            Map<String, List<Ticket>> ticketsByEmail = tickets.stream()
                    .collect(Collectors.groupingBy(Ticket::getEmail));

            // Process each email group
            for (Map.Entry<String, List<Ticket>> entry : ticketsByEmail.entrySet()) {
                String email = entry.getKey();
                List<Ticket> emailTickets = entry.getValue();

                // Sort tickets by ticket type
                emailTickets.sort(Comparator.comparing(Ticket::getTicketType));

                List<BufferedImage> qrCodeImages = new ArrayList<>();
                List<File> qrCodeFiles = new ArrayList<>();

                for (Ticket ticket : emailTickets) {
                    String qrCodeText = String.format("order_id:%s,ticket_id:%s,ticket_type:%s,nic:%s,email:%s,amount:%s,hash:%s",
                            ticket.getOrderId(), ticket.getTicketId(), ticket.getTicketType(), ticket.getNic(),
                            ticket.getEmail(), ticket.getAmount(), ticket.getHash());

                    BufferedImage qrCodeImage = qrCodeService.generateQRCodeImage(qrCodeText);
                    qrCodeImages.add(qrCodeImage);

                    // Save QR code image to a temporary file
                    File qrCodeFile = File.createTempFile("qr_code_" + ticket.getTicketId(), ".png");
                    qrCodeService.saveQRCodeImageToFile(qrCodeImage, qrCodeFile); // Save QR code image to file
                    qrCodeFiles.add(qrCodeFile);
                }

                // Generate PDF with QR codes
                byte[] pdfBytes = pdfGenerationService.generatePDFWithQRCodes(emailTickets, qrCodeImages);
                File pdfFile = File.createTempFile("qr_codes_", ".pdf");
                pdfGenerationService.savePDF(pdfBytes, pdfFile); // Save PDF file

                // Send email with QR code images and PDF attachment
                emailService.sendEmailWithQRCode(email, "Your QR Code Tickets", "Please find your QR code tickets attached.", qrCodeFiles, pdfFile);
            }

            return ResponseEntity.ok("QR codes generated and emails sent successfully.");
        } catch (WriterException | IOException | MessagingException | jakarta.mail.MessagingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate QR codes and send emails.");
        }
    }



    @GetMapping("/downloadQR/{orderId}")
    public ResponseEntity<byte[]> downloadQR(@PathVariable("orderId") Long orderId) {
        try {
            // Fetch tickets for the given order ID and sort by event type
            List<Ticket> tickets = ticketRepository.findByOrderId(orderId);
            tickets.sort(Comparator.comparing(Ticket::getTicketType));

            List<BufferedImage> qrCodeImages = new ArrayList<>();
            for (Ticket ticket : tickets) {
                String qrCodeText = String.format("order_id:%s,ticket_id:%s,ticket_type:%s,nic:%s,email:%s,amount:%s,hash:%s",
                        ticket.getOrderId(), ticket.getTicketId(), ticket.getTicketType(), ticket.getNic(),
                        ticket.getEmail(), ticket.getAmount(), ticket.getHash());

                BufferedImage qrCodeImage = qrCodeService.generateQRCodeImage(qrCodeText);
                qrCodeImages.add(qrCodeImage);
            }

            // Generate PDF with QR codes
            byte[] pdfBytes = pdfGenerationService.generatePDFWithQRCodes(tickets, qrCodeImages);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=qr_codes.pdf")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
