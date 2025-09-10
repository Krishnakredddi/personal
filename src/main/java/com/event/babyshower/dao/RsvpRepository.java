package com.event.babyshower.dao;

import com.event.babyshower.model.Rsvp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RsvpRepository extends JpaRepository<Rsvp, UUID> {
    List<Rsvp> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByCreatedAtDesc(String n, String e);
}

