package lsa.prototype.vem.model.version;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lsa.prototype.vem.model.ILeafEntity;
import lsa.prototype.vem.model.IVersionedEntity;

import java.io.Serializable;

@MappedSuperclass
public class LeafEntity<P extends IVersionedEntity> extends VersionedEntity implements ILeafEntity<P> {
    @ManyToOne
    private P parent;
    private String parentUuid;

    public P getParent() {
        return parent;
    }

    public void setParent(P parent) {
        this.parent = parent;
    }

    public Serializable getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(Serializable parentUuid) {
        this.parentUuid = parentUuid.toString();
    }
}
