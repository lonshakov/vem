package io.persistence.vem.domain.model;

public class Version {
    private VersionState state;
    private String user;

    public Version(VersionState state, String user) {
        this.state = state;
        this.user = user;
    }

    public Version() {
    }

    public VersionState getState() {
        return state;
    }

    public String getUser() {
        return user;
    }
}
