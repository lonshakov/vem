package lsa.prototype.vem.spi.request;

import lsa.prototype.vem.model.Root;
import lsa.prototype.vem.spi.session.VersioningEntityManager;

public interface ChangeRequestSpecificationBuilder {
    <T extends Root> ChangeRequestSpecification<T> build(T root, VersioningEntityManager vem);
}
