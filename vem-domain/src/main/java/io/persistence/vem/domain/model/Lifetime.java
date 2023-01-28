package io.persistence.vem.domain.model;

import java.time.LocalDateTime;

public class Lifetime {
    private LocalDateTime starting = LocalDateTime.now();
    private LocalDateTime expiring = starting.plusYears(1);

    public Lifetime(LocalDateTime starting, LocalDateTime expiring) {
        this.starting = starting;
        this.expiring = expiring;
    }

    public Lifetime() {
    }

    public LocalDateTime getStarting() {
        return starting;
    }

    public LocalDateTime getExpiring() {
        return expiring;
    }
}
