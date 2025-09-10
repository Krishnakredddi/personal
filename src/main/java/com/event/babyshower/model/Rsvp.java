package com.event.babyshower.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Getter @Setter @NoArgsConstructor
@AllArgsConstructor @Builder
public class Rsvp {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String email;
    private String phone;
    private Integer guestCount;

    @Column(length = 2000)
    private String notes;

    private Instant createdAt;
    private String ip;
    @Column(length = 512)
    private String ua;
}
