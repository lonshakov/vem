package lsa.prototype.vem.model.context;

import jakarta.persistence.Embeddable;
import lsa.prototype.vem.model.basic.PersistedObject;

@Embeddable
public class PolymorphEntity {
    private Class<?> type;
    private long id;

    public PolymorphEntity() {
    }

    public PolymorphEntity(Class<?> type, long id) {
        this.type = type;
        this.id = id;
    }

    public PolymorphEntity(PersistedObject object) {
        this.type = object.getClass();
        this.id = object.getId();
    }

    public Class<?> getType() {
        return type;
    }

    public long getId() {
        return id;
    }
}
