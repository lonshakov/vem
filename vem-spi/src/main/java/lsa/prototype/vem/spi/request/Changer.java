package lsa.prototype.vem.spi.request;

import lsa.prototype.vem.model.context.ChangeOperation;
import lsa.prototype.vem.model.context.ChangeRequest;
import lsa.prototype.vem.model.context.ChangeUnit;
import lsa.prototype.vem.model.version.LeafEntity;
import lsa.prototype.vem.model.version.RootEntity;
import lsa.prototype.vem.spi.schema.Datatype;

import java.util.List;
import java.util.stream.Stream;

public interface Changer {
    <T extends RootEntity> ChangeRequest<T> createChangeRequest(T entity);

    <T extends RootEntity> ChangeUnit<ChangeRequest<T>> createChangeUnit(
            ChangeRequest<T> request,
            LeafEntity<?> leaf,
            ChangeOperation operation);

    <T extends RootEntity> Datatype<ChangeRequest<T>> getRequestDatatype(T entity);

    <T extends RootEntity> Datatype<ChangeUnit<ChangeRequest<T>>> getUnitDatatype(T entity);

    <T extends RootEntity> List<ChangeUnit<ChangeRequest<T>>> getUnits(ChangeRequest<T> request);

    <T extends RootEntity> LeafEntity<?> fetch(ChangeUnit<ChangeRequest<T>> unit, boolean lazy);

    <T extends RootEntity> Stream<LeafEntity<?>> stream(ChangeRequest<T> request, boolean batch);
}
