package lsa.prototype.vem.spi.request;

import lsa.prototype.vem.model.ILeafEntity;
import lsa.prototype.vem.model.IRootEntity;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.request.IChangeRequest;
import lsa.prototype.vem.request.IChangeUnit;
import lsa.prototype.vem.spi.schema.Datatype;

import java.util.List;
import java.util.stream.Stream;

public interface Changer {
    <T extends IRootEntity> IChangeRequest<T> createChangeRequest(T entity);

    <T extends IRootEntity> IChangeUnit<IChangeRequest<T>> createChangeUnit(
            IChangeRequest<T> request,
            ILeafEntity<?> leaf,
            ChangeOperation operation);

    <T extends IRootEntity> Datatype<IChangeRequest<T>> getRequestDatatype(T entity);

    <T extends IRootEntity> Datatype<IChangeUnit<IChangeRequest<T>>> getUnitDatatype(T entity);

    <T extends IRootEntity> List<IChangeUnit<IChangeRequest<T>>> getUnits(IChangeRequest<T> request);

    <T extends IRootEntity> ILeafEntity<?> fetch(IChangeUnit<IChangeRequest<T>> unit, boolean lazy);

    <T extends IRootEntity> Stream<ILeafEntity<?>> stream(IChangeRequest<T> request, boolean batch);
}
