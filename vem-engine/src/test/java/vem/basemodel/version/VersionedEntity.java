package vem.basemodel.version;

import io.persistence.vem.domain.model.Lifetime;
import io.persistence.vem.domain.model.Version;
import io.persistence.vem.domain.model.VersionState;
import io.persistence.vem.domain.model.Versionable;
import vem.basemodel.basic.Particle;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
public class VersionedEntity extends Particle implements Versionable {
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "state", column = @Column(name = "version_state")),
            @AttributeOverride(name = "user", column = @Column(name = "version_user"))
    })
    private Version version = new Version(VersionState.DRAFT, null);
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "starting", column = @Column(name = "lifetime_starting_date")),
            @AttributeOverride(name = "expiring", column = @Column(name = "lifetime_expiring_date"))
    })
    private Lifetime lifetime = new Lifetime();
    private long partition;

    public Version getVersion() {
        return version;
    }

    @Override
    public Lifetime getLifetime() {
        return null;
    }

    @Override
    public Serializable getPartition() {
        return partition;
    }
}
