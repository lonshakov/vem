package io.persistence.vem.domain.model;

public class Version {
    private VersionState state;
    private long date;

    public Version(VersionState state, long date) {
        this.state = state;
        this.date = date;
    }

    public Version() {
    }

    public VersionState getState() {
        return state;
    }

    public long getDate() {
        return date;
    }
}
