package vem.basemodel.version;

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
            @AttributeOverride(name = "date", column = @Column(name = "version_date"))
    })
    private Version version = new Version(VersionState.DRAFT, LocalDateTime.MIN);
    private long partition;

    public Version getVersion() {
        return version;
    }

    @Override
    public Serializable getPartition() {
        return partition;
    }
}
