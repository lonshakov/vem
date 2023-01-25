package lsa.prototype.vem.engine.impl.lab;

import lsa.prototype.vem.model.Persistable;
import lsa.prototype.vem.spi.schema.Datatype;
import lsa.prototype.vem.spi.schema.Schema;

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

        Datatype<T> datatype = schema.datatype(entity);
        T clone = datatype.instantiate();
        components.put(entity, clone);

        //copy primitives
        datatype.primitives().values().forEach(parameter -> parameter.set(clone, parameter.get(entity)));

        //copy collections
        datatype.collections().values().forEach(parameter -> {
            List<Persistable> cloneCollection = ((Collection<Persistable>) parameter.get(entity))
                    .stream()
                    .map(leaf -> clone(leaf, schema))
                    //.map(leaf -> wire(leaf, clone, parameter))
                    .toList();
            ((Collection<Persistable>) parameter.get(clone)).addAll(cloneCollection);
        });

        //copy references
        datatype.references().values().stream().forEach(parameter -> {
            Persistable leaf = (Persistable) parameter.get(entity);
            if (leaf != null) {
                Persistable cloneReference = clone(leaf, schema);
                parameter.set(clone, cloneReference);
                //wire(leaf, clone, parameter);
            }
        });
        return clone;
    }

    /*private <T, U> T wire(T child, U parent, Parameter<U> parameter) {
        if (child instanceof Leaf<?>) {
            Object parentUuid = parameter.getStructureDatatype().primitive("uuid").get(parent);
            Datatype<Leaf<?>> datatype = (Datatype<Leaf<?>>) parameter.getParameterDatatype();
            datatype.primitive("parentUuid").set((Leaf<?>) child, parentUuid);
        }
        return child;
    }*/
}
