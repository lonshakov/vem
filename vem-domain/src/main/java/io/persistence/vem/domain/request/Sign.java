package io.persistence.vem.domain.request;

import java.time.LocalDateTime;

public class Sign {
    private String user;
    private LocalDateTime date;

    public Sign() {
    }

    public Sign(String user, LocalDateTime date) {
        this.user = user;
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
