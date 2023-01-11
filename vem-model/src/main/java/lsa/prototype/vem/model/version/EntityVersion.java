package lsa.prototype.vem.model.version;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class EntityVersion {
    @Enumerated(EnumType.STRING)
    @Column(name = "version_state")
    private State state;
    @Column(name = "version_date")
    private long date;

    public EntityVersion() {
    }

    public EntityVersion(State state, long date) {
        this.state = state;
        this.date = date;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public static enum State {
        DRAFT, PURGE, ACTIVE, PASSIVE, HISTORY
    }
}
