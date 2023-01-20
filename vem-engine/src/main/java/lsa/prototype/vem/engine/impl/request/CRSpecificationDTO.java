package lsa.prototype.vem.engine.impl.request;

import lsa.prototype.vem.model.version.RootEntity;
import lsa.prototype.vem.spi.request.ChangeRequestSpecification;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CRSpecificationDTO<T extends RootEntity> implements ChangeRequestSpecification<T> {
    private UUID uuid;
    private T root;
    private Set<Unit> units = new HashSet<>();

    public CRSpecificationDTO(UUID uuid, T root) {
        this.uuid = uuid;
        this.root = root;
    }

    public CRSpecificationDTO() {
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
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
