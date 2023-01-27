package io.persistence.vem.domain.model;

import java.time.LocalDateTime;

public class Version {
    private VersionState state;
    private LocalDateTime date;

    public Version(VersionState state, LocalDateTime date) {
        this.state = state;
        this.date = date;
    }

    public Version() {
    }

    public VersionState getState() {
        return state;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
