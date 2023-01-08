package lsa.prototype.vem.model.context;

import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lsa.prototype.vem.model.basic.Particle;
import lsa.prototype.vem.model.version.Root;

@MappedSuperclass
abstract public class ChangeRequest<T extends Root> extends Particle {
    @ManyToOne(fetch = FetchType.EAGER)
    private T root;
    private State state;

    public T getRoot() {
        return root;
    }

    public void setRoot(T root) {
        this.root = root;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public enum State {
        DRAFT, ACTIVE, APPROVED, DECLINED
    }
}