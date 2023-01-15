package lsa.prototype.vem.model.context;

import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lsa.prototype.vem.model.basic.Particle;
import lsa.prototype.vem.model.version.Root;

@MappedSuperclass
public class ChangeRequest<T extends Root> extends Particle {
    private ChangeRequestState state = new ChangeRequestState(ChangeRequestState.Type.DRAFT, System.currentTimeMillis());
    @ManyToOne(fetch = FetchType.EAGER)
    private T root;

    public T getRoot() {
        return root;
    }

    public void setRoot(T root) {
        this.root = root;
    }

    public ChangeRequestState getState() {
        return state;
    }

    public void setState(ChangeRequestState state) {
        this.state = state;
    }

    public void setState(ChangeRequestState.Type type, long date) {
        this.state = new ChangeRequestState(type, date);
    }
}