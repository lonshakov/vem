package io.persistence.vem.engine.impl.crs;

import io.persistence.vem.domain.model.Root;
import io.persistence.vem.spi.request.ChangeRequestSpecification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CRSpecificationDTO<T extends Root> implements ChangeRequestSpecification<T> {
    private Serializable uuid;
    private T root;
    private List<Unit> units = new ArrayList<>();

    public CRSpecificationDTO(Serializable uuid, T root) {
        this.uuid = uuid;
        this.root = root;
    }

    public CRSpecificationDTO() {
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
    public List<Unit> getUnits() {
        return units;
    }
}
