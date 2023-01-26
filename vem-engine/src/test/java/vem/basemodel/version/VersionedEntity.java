package vem.basemodel.version;

import jakarta.persistence.*;
import lsa.prototype.vem.model.Version;
import lsa.prototype.vem.model.VersionState;
import lsa.prototype.vem.model.Versionable;
import vem.basemodel.basic.Particle;

import java.io.Serializable;

@MappedSuperclass
public class VersionedEntity extends Particle implements Versionable {
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "state", column = @Column(name = "version_state")),
            @AttributeOverride(name = "date", column = @Column(name = "version_date"))
    })
    private Version version = new Version(VersionState.DRAFT, 0);
    private long partition;

    public Version getVersion() {
        return version;
    }

    @Override
    public Serializable getPartition() {
        return partition;
    }
}
