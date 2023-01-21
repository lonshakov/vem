package lsa.prototype.vem.model.version;

import jakarta.persistence.MappedSuperclass;
import lsa.prototype.vem.model.IRootEntity;

@MappedSuperclass
public class RootEntity extends VersionedEntity implements IRootEntity {
}
