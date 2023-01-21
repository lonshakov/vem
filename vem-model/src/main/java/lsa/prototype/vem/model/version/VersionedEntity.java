package lsa.prototype.vem.model.version;

import jakarta.persistence.*;
import lsa.prototype.vem.model.Version;
import lsa.prototype.vem.model.Versionable;
import lsa.prototype.vem.model.basic.Particle;

@MappedSuperclass
public class VersionedEntity extends Particle implements Versionable {
    @AttributeOverrides({
            @AttributeOverride(name = "stateType", column = @Column(name = "version_state")),
            @AttributeOverride(name = "date", column = @Column(name = "version_date"))
    })
    @Embedded
    private Version version = new Version(Version.StateType.DRAFT, System.currentTimeMillis());

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public void setVersion(Version.StateType state, long date) {
        this.version = new Version(state, date);
    }
}
