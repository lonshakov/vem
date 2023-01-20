package lsa.prototype.vem.model.version;

import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.util.UUID;

@MappedSuperclass
public class LeafEntity<P extends VersionedEntity> extends VersionedEntity {
    @ManyToOne
    private P parent;
    @Column(columnDefinition = "uuid")
    private UUID parentUuid;

    public P getParent() {
        return parent;
    }

    public void setParent(P parent) {
        this.parent = parent;
    }

    public UUID getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(UUID parentUuid) {
        this.parentUuid = parentUuid;
    }
}
