package com.event.babyshower.business;

import com.event.babyshower.model.EventProperties;
import com.event.babyshower.model.Rsvp;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;
    private final EventProperties eventProps;

    @Value("${app.owner.email}")
    private String ownerEmail;

    public void sendOwnerNotification(Rsvp r) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(ownerEmail);
        msg.setTo(r.getEmail());
        msg.setSubject("New Baby-Shower RSVP – %s (+%d)".formatted(r.getName(), r.getGuestCount()));
        msg.setText("""
        Event: %s
        Name: %s
        Email: %s
        Phone: %s
        Guests: %d
        Notes: %s
        Submitted: %s
        IP/UA: %s | %s
        Venue: %s — %s
        """.formatted(
                eventProps.getTitle(),
                r.getName(), r.getEmail(), r.getPhone(),
                r.getGuestCount(), nvl(r.getNotes()),
                r.getCreatedAt(), nvl(r.getIp()), nvl(r.getUa()),
                eventProps.getVenue(), eventProps.getAddress()
        ));
        mailSender.send(msg);
    }

    private String nvl(String s){ return s==null?"":s; }
}

