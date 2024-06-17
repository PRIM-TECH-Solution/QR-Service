package com.paymentback.qrgeneration.Repository;

import com.paymentback.qrgeneration.Entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByTicketIdIn(List<String> ticketIds);
    List<Ticket> findByOrderId(Long orderId);

}
