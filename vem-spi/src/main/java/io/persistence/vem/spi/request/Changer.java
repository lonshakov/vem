package io.persistence.vem.spi.request;

import io.persistence.vem.domain.model.Leaf;
import io.persistence.vem.domain.model.Root;
import io.persistence.vem.domain.request.ChangeOperation;
import io.persistence.vem.domain.request.ChangeRequest;
import io.persistence.vem.domain.request.ChangeUnit;
import io.persistence.vem.spi.schema.Datatype;

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
