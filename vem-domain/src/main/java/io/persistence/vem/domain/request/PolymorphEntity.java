package io.persistence.vem.domain.request;

import java.io.Serializable;

public class PolymorphEntity {
    private Class<?> type;
    private Serializable id;

    public PolymorphEntity() {
    }

    public PolymorphEntity(Class<?> type, Serializable id) {
        this.type = type;
        this.id = id;
    }

    public Class<?> getType() {
        return type;
    }

    public Serializable getId() {
        return id;
    }
}
