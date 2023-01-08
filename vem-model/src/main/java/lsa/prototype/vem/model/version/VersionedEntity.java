package lsa.prototype.vem.model.version;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lsa.prototype.vem.model.basic.Particle;

@MappedSuperclass
public class VersionedEntity extends Particle {
    @Enumerated(EnumType.STRING)
    private State versionState;
    private long versionDate;

    public State getVersionState() {
        return versionState;
    }

    public void setVersionState(State versionState) {
        this.versionState = versionState;
    }

    public static enum State {
        DRAFT, PURGE, ACTIVE, PASSIVE, HISTORY
    }
}
