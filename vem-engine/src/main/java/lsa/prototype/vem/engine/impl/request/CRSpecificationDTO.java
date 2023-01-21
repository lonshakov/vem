package lsa.prototype.vem.engine.impl.request;

import lsa.prototype.vem.model.IRootEntity;
import lsa.prototype.vem.request.IChangeRequestSpecification;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class CRSpecificationDTO<T extends IRootEntity> implements IChangeRequestSpecification<T> {
    private Serializable id;
    private Serializable uuid;
    private T root;
    private Set<Unit> units = new HashSet<>();

    public CRSpecificationDTO(T root) {
        this.uuid = root.getUuid();
        this.root = root;
    }

    public CRSpecificationDTO() {
    }

    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public Serializable getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(Serializable uuid) {
        this.uuid = uuid;
    }

    @Override
    public T getRoot() {
        return root;
    }

    public void setRoot(T root) {
        this.root = root;
    }

    @Override
    public Set<Unit> getUnits() {
        return units;
    }
}
