package io.persistence.vem.spi.schema;

import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.model.Versionable;
import io.persistence.vem.domain.request.ChangeUnit;

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
                    Datatype<?> request = unit.getReference("request").getParameterDatatype();
                    Datatype<? extends Root> root = (Datatype<? extends Root>) request.getReference("root").getParameterDatatype();
                    return new HistoryMapping<>(root, request, unit);
                })
                .collect(Collectors.toMap(e -> e.getRootDatatype().getJavaType(), e -> e));


        for (Datatype<?> datatype : schema) {
            Class<?> key = datatype.getJavaType();
            if (Versionable.class.isAssignableFrom(key)) {
                mappings.putIfAbsent(key, distinct.get(getRoot(key, schema)));
            }
        }
    }

    public <V extends Versionable> HistoryMapping<?> get(Class<V> type) {
        return mappings.get(type);
    }

    public <V extends Versionable> HistoryMapping<?> get(V object) {
        return get(object.getClass());
    }

    private Class<?> getRoot(Class key, Schema schema) {
        if (Root.class.isAssignableFrom(key)) {
            return key;
        }

        return getRoot(schema.getDatatype(key).getReference("parent").getParameterDatatype().getJavaType(), schema);
    }
}
