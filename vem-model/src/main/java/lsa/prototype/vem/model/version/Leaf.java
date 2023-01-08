package lsa.prototype.vem.model.version;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class Leaf<P extends VersionedEntity> extends VersionedEntity {
    @ManyToOne
    private P parent;

    public P getParent() {
        return parent;
    }

    public void setParent(P parent) {
        this.parent = parent;
    }
}
