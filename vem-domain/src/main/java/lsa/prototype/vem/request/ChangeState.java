package lsa.prototype.vem.request;

public class ChangeState {
    private StateType type = StateType.DRAFT;
    private long date;

    public ChangeState() {
    }

    public ChangeState(StateType type, long date) {
        this.type = type;
        this.date = date;
    }

    public StateType getStateType() {
        return type;
    }

    public void setStateType(StateType type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public enum StateType {
        DRAFT, PUBLISHED, AFFIRMED, REJECTED
    }
}
