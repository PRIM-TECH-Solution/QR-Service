package com.paymentback.qrgeneration.Controller;

import com.paymentback.qrgeneration.Entity.Ticket;
import com.paymentback.qrgeneration.Service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/create")
    public ResponseEntity<List<Ticket>> createTickets(@RequestBody List<Ticket> tickets) {
        List<Ticket> createdTickets = ticketService.createTickets(tickets);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTickets);
    }

    // You can add more endpoints for other ticket operations if needed
}
