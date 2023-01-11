package lsa.prototype.vem.model.version;

import jakarta.persistence.MappedSuperclass;
import lsa.prototype.vem.model.basic.Particle;

@MappedSuperclass
public class VersionedEntity extends Particle {
    private EntityVersion version = new EntityVersion(EntityVersion.State.DRAFT, System.currentTimeMillis());

    public EntityVersion getVersion() {
        return version;
    }

    public void setVersion(EntityVersion version) {
        this.version = version;
    }
}
