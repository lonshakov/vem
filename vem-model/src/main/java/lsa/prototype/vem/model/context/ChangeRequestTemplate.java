package lsa.prototype.vem.model.context;

import jakarta.persistence.*;
import lsa.prototype.vem.model.basic.Particle;
import lsa.prototype.vem.model.version.RootEntity;
import lsa.prototype.vem.request.ChangeRequest;
import lsa.prototype.vem.request.ChangeState;
import lsa.prototype.vem.request.Sign;

@MappedSuperclass
public class ChangeRequestTemplate<T extends RootEntity> extends Particle implements ChangeRequest<T> {
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "request_state")),
            @AttributeOverride(name = "date", column = @Column(name = "request_date"))
    })
    @Embedded
    private ChangeState state = new ChangeState(ChangeState.StateType.DRAFT, System.currentTimeMillis());
    @ManyToOne(fetch = FetchType.EAGER)
    private T root;
    @AttributeOverrides({
            @AttributeOverride(name = "user", column = @Column(name = "creation_user")),
            @AttributeOverride(name = "date", column = @Column(name = "creation_date"))
    })
    @Embedded
    private Sign creationSign;
    @AttributeOverrides({
            @AttributeOverride(name = "user", column = @Column(name = "solution_user")),
            @AttributeOverride(name = "date", column = @Column(name = "solution_date"))
    })
    @Embedded
    private Sign solutionSing;

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

    public Sign getCreationSign() {
        return creationSign;
    }

    public void setCreationSign(Sign creationSign) {
        this.creationSign = creationSign;
    }

    public Sign getSolutionSing() {
        return solutionSing;
    }

    public void setSolutionSing(Sign solutionSing) {
        this.solutionSing = solutionSing;
    }
}