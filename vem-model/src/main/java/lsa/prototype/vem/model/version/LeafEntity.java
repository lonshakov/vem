package lsa.prototype.vem.model.version;

import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.util.UUID;

@MappedSuperclass
//@Index(name = "affinity_idx", columnList = "affinity")
public class LeafEntity<P extends VersionedEntity> extends VersionedEntity {
    @ManyToOne
    private P parent;
    @Column(columnDefinition = "uuid")
    private UUID affinity;

    public P getParent() {
        return parent;
    }

    public void setParent(P parent) {
        this.parent = parent;
    }

    public UUID getAffinity() {
        return affinity;
    }

    public void setAffinity(UUID affinity) {
        this.affinity = affinity;
    }
}
