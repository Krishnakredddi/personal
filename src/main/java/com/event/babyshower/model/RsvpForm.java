package com.event.babyshower.model;

import jakarta.validation.constraints.*;

public record RsvpForm(
        @NotBlank String name,
        @Email @NotBlank String email,
        String phone,
        @NotNull @Min(1) Integer guestCount,
        @Size(max=2000) String notes
) {}

