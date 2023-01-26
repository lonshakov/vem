package vem.basemodel.basic;

import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.UUID;

@MappedSuperclass
public class Particle extends PersistedObject {
    private String uuid = UUID.randomUUID().toString();

    public Serializable getUuid() {
        return uuid;
    }

    public void setUuid(Serializable uuid) {
        this.uuid = uuid.toString();
    }
}
