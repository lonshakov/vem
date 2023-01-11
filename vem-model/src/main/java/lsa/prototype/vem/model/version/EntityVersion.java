package lsa.prototype.vem.model.version;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class EntityVersion {
    @Enumerated(EnumType.STRING)
    @Column(name = "version_state")
    private StateType stateType;
    @Column(name = "version_date")
    private long date;

    public EntityVersion() {
    }

    public EntityVersion(StateType stateType, long date) {
        this.stateType = stateType;
        this.date = date;
    }

    public StateType getStateType() {
        return stateType;
    }

    public void setStateType(StateType state) {
        this.stateType = state;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public static enum StateType {
        DRAFT, PURGE, ACTIVE, PASSIVE, HISTORY
    }
}
