package lsa.prototype.vem.model.context;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class ChangeState {
    @Enumerated(EnumType.STRING)
    @Column(name = "request_state")
    private StateType type = StateType.DRAFT;
    @Column(name = "request_date")
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
        DRAFT, ACTIVE, APPROVED, DECLINED
    }
}
