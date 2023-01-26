package vem.basemodel.request;

import io.persistence.vem.domain.request.ChangeRequest;
import io.persistence.vem.domain.request.ChangeState;
import io.persistence.vem.domain.request.Sign;
import vem.basemodel.basic.Particle;
import vem.basemodel.version.RootEntity;

import javax.persistence.*;

@MappedSuperclass
public class ChangeRequestTemplate<T extends RootEntity> extends Particle implements ChangeRequest<T> {
    private ChangeState state = ChangeState.DRAFT;
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