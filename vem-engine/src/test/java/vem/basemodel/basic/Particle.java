package vem.basemodel.basic;

import io.persistence.vem.domain.model.GlobalId;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.UUID;

@MappedSuperclass
public class Particle extends PersistedObject {
    @GlobalId
    private String customUuid = UUID.randomUUID().toString();

    public Serializable getUuid() {
        return customUuid;
    }

    public void setUuid(Serializable uuid) {
        this.customUuid = uuid.toString();
    }
}
