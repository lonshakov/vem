package lsa.prototype.vem.engine.impl.schema;

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
        public void set(Object entity, Object value) {
            persister.getEntityTuplizer().setIdentifier(entity, (Serializable) value);
        }

        @Override
        public Object get(Object entity) {
            return persister.getEntityTuplizer().getIdentifier(entity);
        }
    }

    public static class Primitive implements Accessor {
        private final EntityPersister persister;
        private final int index;

        public Primitive(EntityPersister persister, String name) {
            this.persister = persister;
            index = persister.getEntityMetamodel().getPropertyIndex(name);
        }

        @Override
        public void set(Object entity, Object value) {
            persister.setPropertyValue(entity, index, value);
        }

        @Override
        public Object get(Object entity) {
            return persister.getPropertyValue(entity, index);
        }
    }
}
