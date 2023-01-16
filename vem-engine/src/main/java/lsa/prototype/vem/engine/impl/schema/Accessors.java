package lsa.prototype.vem.engine.impl.schema;

import lsa.prototype.vem.model.basic.PersistedObject;
import lsa.prototype.vem.spi.schema.Accessor;
import org.hibernate.persister.entity.EntityPersister;

import java.io.Serializable;

public class Accessors {
    public static class Id implements Accessor {
        private final EntityPersister persister;

        public Id(EntityPersister persister) {
            this.persister = persister;
        }

        @Override
        public void set(PersistedObject entity, Serializable value) {
            persister.getEntityTuplizer().setIdentifier(entity, value);
        }

        @Override
        public Serializable get(PersistedObject entity) {
            return persister.getEntityTuplizer().getIdentifier(entity);
        }
    }

    public static class Parameter implements Accessor {
        private final EntityPersister persister;
        private final int index;

        public Parameter(EntityPersister persister, String name) {
            this.persister = persister;
            index = persister.getEntityMetamodel().getPropertyIndex(name);
        }

        @Override
        public void set(PersistedObject entity, Serializable value) {
            persister.setPropertyValue(entity, index, value);
        }

        @Override
        public Serializable get(PersistedObject entity) {
            return (Serializable) persister.getPropertyValue(entity, index);
        }
    }
}
