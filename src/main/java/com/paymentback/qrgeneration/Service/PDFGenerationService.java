package com.paymentback.qrgeneration.Service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Line;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.paymentback.qrgeneration.Entity.Ticket;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class PDFGenerationService {

    public byte[] generatePDFWithQRCodes(List<Ticket> tickets, List<BufferedImage> qrCodeImages) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer)) {

            // Set the page size to A4
            pdf.setDefaultPageSize(PageSize.A4);

            try (Document document = new Document(pdf)) {

                for (int i = 0; i < qrCodeImages.size(); i++) {
                    BufferedImage qrCodeImage = qrCodeImages.get(i);
                    Ticket ticket = tickets.get(i);

                    // Create an image object from the QR code
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(qrCodeImage, "png", byteArrayOutputStream);
                    Image pdfImage = new Image(ImageDataFactory.create(byteArrayOutputStream.toByteArray()));

                    // Scale down the QR code image
                    pdfImage.setWidth(100); // Set the desired width
                    pdfImage.setHeight(100); // Set the desired height
                    pdfImage.setHorizontalAlignment(HorizontalAlignment.CENTER);

                    PdfFont bold = PdfFontFactory.createFont("Helvetica-Bold");
                    PdfFont regular = PdfFontFactory.createFont("Helvetica");


                    // Set the background color based on ticket type
                    Color backgroundColor = getBackgroundColor(ticket.getTicketType());

                    // Create a Table with one cell to hold the QR code image
                    Table table = new Table(1);
                    table.setWidth(UnitValue.createPercentValue(70));
                    table.setBackgroundColor(backgroundColor);
                    table.setHorizontalAlignment(HorizontalAlignment.CENTER);
//                    table.setMarginTop();
                    table.addCell(pdfImage.setAutoScale(true).setMarginTop(40).setMarginBottom(40).setMarginLeft(1).setMarginRight(1));

                    // Add the ticket type (uppercase) above the QR code
                    Paragraph ticketTypeParagraph = new Paragraph(ticket.getTicketType().toUpperCase())
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontColor(ColorConstants.BLACK)
                            .setBackgroundColor(getBackgroundColor(ticket.getTicketType()))
                            .setFont(bold)
                            .setMarginTop(10)
                            .setMarginBottom(5)
                            .setFontSize(40)
                            .setBold();

                    Paragraph headerParagraph = new Paragraph("THIS IS YOUR E-TICKET" )
                            .setFont(bold)
                            .setFontSize(24)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontColor(ColorConstants.BLACK)
                            .setBackgroundColor(getBackgroundColor(ticket.getTicketType()))
                            .setBorderTop(new SolidBorder(ColorConstants.BLACK, 1)) // Add top border
                            .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1)) // Add bottom border
                            .setMarginBottom(10);


                   Paragraph noteParagraph = (new Paragraph("Please Show This QR Code On Your Mobile When You Arrive At The Event Location")
                            .setFont(regular)
                            .setFontSize(12)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginBottom(10));

                    Paragraph eventParagraph = (new Paragraph(ticket.getEventName())
                            .setFont(bold)
                            .setFontSize(40)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginBottom(0));

                    Paragraph dateAndTimeParagraph = (new Paragraph(ticket.getEventDate() +"   "+ ticket.getEventTime())
                            .setFont(regular)
                            .setFontSize(15)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginBottom(8));

                    Paragraph copyWriteParagraph = (new Paragraph("This QR code is automatically generated © EasyTicket.LK")
                            .setFont(regular)
                            .setFontSize(8)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginTop(20)
                            .setMarginBottom(5));



                    // Add the ticket number below the QR code
                    Paragraph ticketNumberParagraph = new Paragraph("Ticket Number: " + ticket.getTicketId())
                            .setFont(regular)
                            .setTextAlignment(TextAlignment.CENTER);


                    document.add(headerParagraph);
                    document.add(noteParagraph);
                    document.add(eventParagraph);
                    document.add(dateAndTimeParagraph);
                    document.add(table);
                    document.add(ticketNumberParagraph);
                    document.add(ticketTypeParagraph);
                    document.add(copyWriteParagraph);
                    document.add(new AreaBreak());
                }

                PdfFont bold = PdfFontFactory.createFont("Helvetica-Bold");
                PdfFont regular = PdfFontFactory.createFont("Helvetica");

                // Add a new page with additional text
                // Create a composite paragraph with different color segments
                Paragraph compositeParagraph = new Paragraph()
                        .setMarginLeft(10)
                        .setFontSize(30)
                        .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1))
                        .setMarginBottom(10)
                        .setFont(bold)
                        .setTextAlignment(TextAlignment.CENTER);


                // Add the first part with RGB (255, 163, 26) color
                Text part1 = new Text("Easy ")
                        .setFontColor(new DeviceRgb(255, 163, 26))
                        .setBold();

                compositeParagraph.add(part1);

                // Add the second part with white color
                Text part2 = new Text("Tickets.LK")
                        .setFontColor(ColorConstants.WHITE)
                        .setBackgroundColor(ColorConstants.BLACK)
                        .setBold();
                compositeParagraph.add(part2);


                Paragraph terms = new Paragraph("Terms & Conditions")
                        .setFont(bold)
                        .setMarginTop(10)
                        .setMarginLeft(5)
                        .setFontSize(20)
                        .setMarginBottom(5)
                        .setTextAlignment(TextAlignment.LEFT);


                Paragraph termsText = new Paragraph("All tickets purchased are non-refundable.\n" +
                        "Please note that our online tickets cannot be changed once purchased. This includes changes to the category, show, seat, price, or any other aspects of the ticket. We highly recommend that you carefully review your selection before completing your online purchase to ensure that you have chosen the correct ticket.\n" +
                        "Please note that online tickets are only available for redemption from one hour before the start of the event until one hour after the event has started. EasyTicket.LK will not issue online tickets after this time and cannot be held responsible for any inconvenience caused. To ensure you have enough time to enjoy the event, please make sure to arrive at the venue on time.\n" +
                        "Only the initial email or SMS provided by EasyTicket.LK will be accepted as proof of purchase. Tickets will not be redeemed for any forwarded or screenshots.\n" +
                        "Valid NIC or Passport will be required if needed during the process of redeeming.\n" +
                        "If available, kindly choose the option for ticket delivery for the appropriate event to avoid the line-up at the entrance for redemption.\n" +
                        "EasyTicket.LK shall not be held accountable for any inconvenience caused in the organization of the concert")
                        .setFont(regular)
                        .setMarginLeft(10)
                        .setMarginRight(10)
                        .setTextAlignment(TextAlignment.JUSTIFIED_ALL)
                        .setFontSize(15)
                        .setTextAlignment(TextAlignment.LEFT);






                document.add(compositeParagraph);
                document.add(terms);
                document.add(termsText);
            }
        }

        return baos.toByteArray();
    }

    public void savePDF(byte[] pdfBytes, File file) throws IOException {
        FileUtils.writeByteArrayToFile(file, pdfBytes);
    }

    private Color getBackgroundColor(String ticketType) {
        switch (ticketType.toLowerCase()) {
            case "vip":
                return new DeviceRgb(255, 215, 0); // Light orange
            case "regular":
                return new DeviceRgb(192, 192, 192); // Light blue
            default:
                return new DeviceRgb(255, 255, 255); // White
        }
    }
}
