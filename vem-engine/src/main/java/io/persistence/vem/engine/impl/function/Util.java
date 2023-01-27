package io.persistence.vem.engine.impl.function;

import io.persistence.vem.domain.model.*;
import io.persistence.vem.domain.request.ChangeOperation;
import io.persistence.vem.engine.impl.crs.CRSpecificationUnitDTO;
import io.persistence.vem.spi.function.Axis;
import io.persistence.vem.spi.function.VisitorContext;
import io.persistence.vem.spi.request.ChangeRequestSpecification;
import io.persistence.vem.spi.schema.Datatype;
import io.persistence.vem.spi.schema.Parameter;
import io.persistence.vem.spi.session.VersioningEntityManager;

import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Util {
    public static <T extends Root, V extends Versionable>
    void defineChangeOperationCascade(V entity, VersioningEntityManager vem, ChangeRequestSpecification<T> specification, ChangeOperation operation) {
        Datatype<V> datatype = vem.getSchema().getDatatype(entity);
        Serializable parentUuid = vem.getSchema().getUtil().getUuid(entity);

        //collections
        for (Parameter<V> parameter : datatype.getCollections().values()) {
            Datatype<Leaf<?>> parameterDatatype = (Datatype<Leaf<?>>) parameter.getParameterDatatype();

            for (Leaf<?> leaf : (Iterable<Leaf<?>>) parameter.get(entity)) {
                specification.getUnits().add(new CRSpecificationUnitDTO(
                        operation,
                        leaf
                ));
                parameterDatatype.getPrimitive("parentUuid").set(leaf, parentUuid);
                defineChangeOperationCascade(
                        leaf,
                        vem,
                        specification,
                        operation
                );
            }
        }

        //references
        for (Parameter<V> parameter : datatype.getReferences().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            Leaf<Versionable> leaf = (Leaf<Versionable>) parameter.get(entity);
            if (leaf == null) {
                continue;
            }
            Datatype<Leaf<?>> parameterDatatype = (Datatype<Leaf<?>>) parameter.getParameterDatatype();
            specification.getUnits().add(new CRSpecificationUnitDTO(
                    operation,
                    leaf
            ));
            parameterDatatype.getPrimitive("parentUuid").set(leaf, parentUuid);
            defineChangeOperationCascade(
                    leaf,
                    vem,
                    specification,
                    operation
            );
        }
    }

    public static <V> void walk(V entity, VisitorContextImpl ctx, BiConsumer<Object, VisitorContext> task) {
        Datatype<V> datatype = ctx.vem().getSchema().getDatatype(entity);
        if (ctx.isVisited(entity))
            return;

        for (Parameter<V> parameter : datatype.getCollections().values()) {
            Axis<?> axis = new Axis<>(entity, parameter);
            for (Leaf<?> leaf : (Iterable<Leaf<?>>) parameter.get(entity)) {
                ctx.register(leaf, axis);
                walk(leaf, ctx, task);
            }
        }
        for (Parameter<V> parameter : datatype.getReferences().values()) {
            if (parameter.getName().equals("parent")) {
                continue;
            }
            Axis<?> axis = new Axis<>(entity, parameter);
            Leaf<?> leaf = (Leaf<?>) parameter.get(entity);
            if (leaf != null) {
                ctx.register(leaf, axis);
                walk(leaf, ctx, task);
            }
        }
        task.accept(entity, ctx);
    }

    public static class VisitorContextImpl implements VisitorContext {
        private final VersioningEntityManager vem;
        private final Map<Object, Axis<?>> register = new IdentityHashMap<>();

        public VisitorContextImpl(VersioningEntityManager vem) {
            this.vem = vem;
        }

        @Override
        public VersioningEntityManager vem() {
            return vem;
        }

        @Override
        public <T, U> Axis<U> getAxis(T entity) {
            return (Axis<U>) register.get(entity);
        }

        @Override
        public boolean isVisited(Object entity) {
            return register.containsKey(entity);
        }

        void register(Object entity, Axis<?> axis) {
            register.put(entity, axis);
        }
    }
}
