package lsa.prototype.vem.model.version;

import jakarta.persistence.*;
import lsa.prototype.vem.model.EntityVersion;
import lsa.prototype.vem.model.IVersionedEntity;
import lsa.prototype.vem.model.basic.Particle;

@MappedSuperclass
public class VersionedEntity extends Particle implements IVersionedEntity {
    @AttributeOverrides({
            @AttributeOverride(name = "stateType", column = @Column(name = "version_state")),
            @AttributeOverride(name = "date", column = @Column(name = "version_date"))
    })
    @Embedded
    private EntityVersion version = new EntityVersion(EntityVersion.StateType.DRAFT, System.currentTimeMillis());

    public EntityVersion getVersion() {
        return version;
    }

    public void setVersion(EntityVersion version) {
        this.version = version;
    }

    public void setVersion(EntityVersion.StateType state, long date) {
        this.version = new EntityVersion(state, date);
    }
}
