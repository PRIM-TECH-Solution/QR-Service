package com.paymentback.qrgeneration.Service;

import com.paymentback.qrgeneration.Entity.Ticket;
import com.paymentback.qrgeneration.Repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    @Autowired
    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<Ticket> createTickets(List<Ticket> tickets) {
        LocalDateTime now = LocalDateTime.now();
        int count = 1; // Initial ticket count

        for (Ticket ticket : tickets) {
            String ticketId = generateTicketId(now, count);
            ticket.setTicketId(ticketId);

            // Generate hash
            String hash = generateHash(ticket.getOrderId(), ticket.getNic(), ticket.getAmount(), ticket.getTicketId());
            ticket.setHash(hash);

            count++;
        }

        return ticketRepository.saveAll(tickets);
    }

    private String generateTicketId(LocalDateTime dateTime, int count) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedDateTime = dateTime.format(formatter);
        return formattedDateTime + "_" + count;
    }

    private String generateHash(Long orderId, String nic, BigDecimal amount, String ticketId) {
        String input = orderId + nic + amount + ticketId;
        return getMd5(input);
    }

    private String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
