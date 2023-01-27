package io.persistence.vem.engine.impl.function;

import io.persistence.vem.spi.schema.Datatype;
import io.persistence.vem.spi.schema.Schema;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class Cloner {
    private Map<Object, Object> components = new IdentityHashMap<>();

    public <T> T clone(T entity, Schema schema) {
        T existed = (T) components.get(entity);
        if (existed != null)
            return existed;

        Datatype<T> datatype = schema.getDatatype(entity);
        T clone = datatype.instantiate();
        components.put(entity, clone);

        //copy primitives
        datatype.getPrimitives().values().stream().filter(p -> !p.getName().equals("version")).forEach(parameter -> {
            parameter.set(clone, parameter.get(entity));
        });
        if (datatype.isGlobal()) {
            datatype.getGlobalIdentifier().set(clone, schema.getUtil().getUuid(entity));
        }

        //copy collections
        datatype.getCollections().values().forEach(parameter -> {
            List<Object> cloneCollection = ((Collection<Object>) parameter.get(entity))
                    .stream()
                    .map(leaf -> clone(leaf, schema))
                    .toList();
            ((Collection<Object>) parameter.get(clone)).addAll(cloneCollection);
        });

        //copy references
        datatype.getReferences().values().forEach(parameter -> {
            Object reference = parameter.get(entity);
            if (reference != null) {
                Object cloneReference = clone(reference, schema);
                parameter.set(clone, cloneReference);
            }
        });
        return clone;
    }
}
