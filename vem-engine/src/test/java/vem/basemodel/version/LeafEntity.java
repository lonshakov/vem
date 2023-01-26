package vem.basemodel.version;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import lsa.prototype.vem.model.Leaf;
import lsa.prototype.vem.model.Versionable;

import java.io.Serializable;

@MappedSuperclass
public class LeafEntity<P extends Versionable> extends VersionedEntity implements Leaf<P> {
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
