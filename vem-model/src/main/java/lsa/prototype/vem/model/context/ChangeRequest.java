package lsa.prototype.vem.model.context;

import jakarta.persistence.*;
import lsa.prototype.vem.model.basic.Particle;
import lsa.prototype.vem.model.version.RootEntity;

@MappedSuperclass
public class ChangeRequest<T extends RootEntity> extends Particle {
    private ChangeState state = new ChangeState(ChangeState.StateType.DRAFT, System.currentTimeMillis());
    @ManyToOne(fetch = FetchType.EAGER)
    private T root;
    @AttributeOverrides({
            @AttributeOverride(name = "user", column = @Column(name = "creation_user")),
            @AttributeOverride(name = "date", column = @Column(name = "creation_date"))
    })
    private Sign creationSign;
    @AttributeOverrides({
            @AttributeOverride(name = "user", column = @Column(name = "solution_user")),
            @AttributeOverride(name = "date", column = @Column(name = "solution_date"))
    })
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