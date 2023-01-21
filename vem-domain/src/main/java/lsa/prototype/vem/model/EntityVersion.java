package lsa.prototype.vem.model;

public class EntityVersion {
    private StateType stateType;
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

    public enum StateType {
        DRAFT, PURGE, ACTIVE, PASSIVE, HISTORY
    }
}
