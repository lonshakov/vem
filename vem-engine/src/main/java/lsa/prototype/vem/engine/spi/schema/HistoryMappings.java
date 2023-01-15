package lsa.prototype.vem.engine.spi.schema;

import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.Root;
import lsa.prototype.vem.model.version.VersionedEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class HistoryMappings {
    private ConcurrentHashMap<Class<?>, HistoryMapping<?>> mappings = new ConcurrentHashMap<>();

    public HistoryMappings(Schema schema) {
        Map<Class<?>, HistoryMapping<?>> distinct = StreamSupport.stream(schema.spliterator(), false)
                .filter(o -> ChangeUnit.class.isAssignableFrom(o.getJavaType()))
                .map(unit -> {
                    Datatype<?> request = unit.reference("request").getParameterDatatype();
                    Datatype<? extends Root> root = (Datatype<? extends Root>) request.reference("root").getParameterDatatype();
                    return new HistoryMapping<>(root, request, unit);
                })
                .collect(Collectors.toMap(e -> e.getRootDatatype().getJavaType(), e -> e));


        for (Datatype<?> datatype : schema) {
            Class<?> key = datatype.getJavaType();
            if (VersionedEntity.class.isAssignableFrom(key)) {
                mappings.putIfAbsent(key, distinct.get(getRoot(key, schema)));
            }
        }
    }

    public <V extends VersionedEntity> HistoryMapping<?> get(Class<V> type) {
        return mappings.get(type);
    }

    public <V extends VersionedEntity> HistoryMapping<?> get(V object) {
        return get(object.getClass());
    }

    private Class<?> getRoot(Class key, Schema schema) {
        if (Root.class.isAssignableFrom(key)) {
            return key;
        }

        return getRoot(schema.datatype(key).reference("parent").getParameterDatatype().getJavaType(), schema);
    }
}
