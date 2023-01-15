package lsa.prototype.vem.model.context;

import jakarta.persistence.Embeddable;

@Embeddable
public class ChangeRequestState {
    private Type type = Type.DRAFT;
    private long date;

    public ChangeRequestState() {
    }

    public ChangeRequestState(Type type, long date) {
        this.type = type;
        this.date = date;
    }

    public Type getStateType() {
        return type;
    }

    public void setStateType(Type type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public enum Type {
        DRAFT, ACTIVE, APPROVED, DECLINED
    }
}
