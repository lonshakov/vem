package lsa.prototype.vem.spi.request;

import lsa.prototype.vem.model.Leaf;
import lsa.prototype.vem.model.Root;
import lsa.prototype.vem.request.ChangeOperation;
import lsa.prototype.vem.request.ChangeRequest;
import lsa.prototype.vem.request.ChangeUnit;
import lsa.prototype.vem.spi.schema.Datatype;

import java.util.List;
import java.util.stream.Stream;

public interface Changer {
    <T extends Root> ChangeRequest<T> createChangeRequest(T entity);

    <T extends Root> ChangeUnit<ChangeRequest<T>> createChangeUnit(
            ChangeRequest<T> request,
            Leaf<?> leaf,
            ChangeOperation operation);

    <T extends Root> Datatype<ChangeRequest<T>> getRequestDatatype(T entity);

    <T extends Root> Datatype<ChangeUnit<ChangeRequest<T>>> getUnitDatatype(T entity);

    <T extends Root> List<ChangeUnit<ChangeRequest<T>>> getStoredChangeUnits(ChangeRequest<T> request);

    <T extends Root> ChangeRequestSpecification.Unit fetch(ChangeUnit<ChangeRequest<T>> unit, boolean lazy);

    <T extends Root> Stream<ChangeRequestSpecification.Unit> stream(ChangeRequest<T> request, boolean lazy);
}
