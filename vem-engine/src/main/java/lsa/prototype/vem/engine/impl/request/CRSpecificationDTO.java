package lsa.prototype.vem.engine.impl.request;

import lsa.prototype.vem.model.Root;
import lsa.prototype.vem.request.ChangeRequestSpecification;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class CRSpecificationDTO<T extends Root> implements ChangeRequestSpecification<T> {
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
    public T getRoot() {
        return root;
    }

    @Override
    public Set<Unit> getUnits() {
        return units;
    }
}
