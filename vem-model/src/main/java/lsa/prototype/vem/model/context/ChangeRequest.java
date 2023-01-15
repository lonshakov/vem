package lsa.prototype.vem.model.context;

import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lsa.prototype.vem.model.basic.Particle;
import lsa.prototype.vem.model.version.Root;

@MappedSuperclass
public class ChangeRequest<T extends Root> extends Particle {
    private ChangeState state = new ChangeState(ChangeState.StateType.DRAFT, System.currentTimeMillis());
    @ManyToOne(fetch = FetchType.EAGER)
    private T root;

    public T getRoot() {
        return root;
    }

    public void setRoot(T root) {
        this.root = root;
    }

    public ChangeState getState() {
        return state;
    }

    public void setState(ChangeState state) {
        this.state = state;
    }

    public void setState(ChangeState.StateType type, long date) {
        this.state = new ChangeState(type, date);
    }
}