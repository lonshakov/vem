package lsa.prototype.vem.spi.session;

import lsa.prototype.vem.model.Persistable;

public interface WalkContext {
    VersioningEntityManager vem();

    Persistable getParent(Persistable entity);

    boolean isWalked(Persistable entity);
}
