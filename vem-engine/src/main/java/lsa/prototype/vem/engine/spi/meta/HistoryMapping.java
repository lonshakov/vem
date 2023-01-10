package lsa.prototype.vem.engine.spi.meta;

import lsa.prototype.vem.model.basic.PersistedObject;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.Root;
import lsa.prototype.vem.model.version.VersionedEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class HistoryMapping {
    private ConcurrentHashMap<Class<?>, Entry<?>> mappings = new ConcurrentHashMap<>();

    public HistoryMapping(Meta meta) {
        Map<Class<?>, Entry<?>> distinct = StreamSupport.stream(meta.spliterator(), false)
                .filter(o -> ChangeUnit.class.isAssignableFrom(o.getJavaType()))
                .map(unit -> {
                    Datatype<?> request = unit.reference("request").getParameterDatatype();
                    Datatype<? extends Root> root = (Datatype<? extends Root>) request.reference("root").getParameterDatatype();
                    return new Entry<>(root, request, unit);
                })
                .collect(Collectors.toMap(e -> e.root().getJavaType(), e -> e));


        for (Datatype<?> datatype : meta) {
            Class<?> key = datatype.getJavaType();
            if (VersionedEntity.class.isAssignableFrom(key)) {
                mappings.putIfAbsent(key, distinct.get(getRoot(key, meta)));
            }
        }
    }

    public <T extends VersionedEntity> Entry<?> get(Class<T> type) {
        return mappings.get(type);
    }

    public <T extends VersionedEntity> Entry<?> get(T object) {
        return get(object.getClass());
    }

    private Class<?> getRoot(Class key, Meta meta) {
        if (Root.class.isAssignableFrom(key)) {
            return key;
        }

        return getRoot(meta.datatype(key).reference("parent").getParameterDatatype().getJavaType(), meta);
    }


    <T extends PersistedObject> Entry<?> getMapping(Class<T> type) {
        return mappings.get(type);
    }

    public static class Entry<T extends Root> {
        private final Datatype<T> root;
        private final Datatype<?> request;
        private final Datatype<?> unit;

        public Entry(Datatype<T> root, Datatype<?> request, Datatype<?> unit) {
            this.root = root;
            this.request = request;
            this.unit = unit;
        }

        public Datatype<T> root() {
            return root;
        }

        public <C extends ChangeRequest<T>> Datatype<C> request() {
            return (Datatype<C>) request;
        }

        public <U extends ChangeUnit<T>> Datatype<U> unit() {
            return (Datatype<U>) unit;
        }
    }
}
