package lsa.prototype.vem.model.basic;

import jakarta.persistence.MappedSuperclass;

import java.util.UUID;

@MappedSuperclass
public class Particle extends PersistedObject {
    private UUID uuid;

    public UUID getUuid() {
        return uuid;
    }
}
